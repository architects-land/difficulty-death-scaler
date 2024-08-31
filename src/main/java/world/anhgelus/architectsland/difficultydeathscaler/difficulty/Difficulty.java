package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Difficulty {
    protected final Timer timer = new Timer();
    protected long timerStart = System.currentTimeMillis() / 1000;
    private TimerTask reducerTask;
    protected Step[] steps;

    protected int numberOfDeath;
    protected final MinecraftServer server;

    protected Difficulty(MinecraftServer server) {
        this.server = server;
        numberOfDeath = 0;
    }

    /**
     * Types of update
     */
    protected enum UpdateType {
        /**
         * Automatic increase
         */
        INCREASE,
        /**
         * Automatic decrease
         */
        DECREASE,
        /**
         * Manual set
         */
        SET,
        /**
         * Silent update
         */
        SILENT
    }

    public static final class Updater {
        private int difficultyLevel = 1;

        private final Map<Class<? extends Modifier<?>>, Modifier<?>> map = new HashMap<>();

        public static final Map<net.minecraft.world.Difficulty, Integer> DIFFICULTY_LEVEL = Map.of(
                net.minecraft.world.Difficulty.PEACEFUL, 0,
                net.minecraft.world.Difficulty.EASY, 1,
                net.minecraft.world.Difficulty.NORMAL, 2,
                net.minecraft.world.Difficulty.HARD, 3
        );

        public void updateDifficulty(int level) {
            if (level > difficultyLevel) difficultyLevel = level;
        }

        public Modifier<?> getModifier(Class<? extends Modifier<?>> clazz) {
            Modifier<?> val = map.get(clazz);
            if (val != null) return val;
            try {
                val = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            map.put(clazz, val);
            return val;
        }

        public List<Modifier<?>> getModifiers() {
            return new ArrayList<>(map.values());
        }

        public net.minecraft.world.Difficulty getDifficulty() {
            return switch (difficultyLevel) {
                case 0 -> net.minecraft.world.Difficulty.PEACEFUL;
                case 1 -> net.minecraft.world.Difficulty.EASY;
                case 2 -> net.minecraft.world.Difficulty.NORMAL;
                case 3 -> net.minecraft.world.Difficulty.HARD;
                default -> throw new IllegalArgumentException("Difficulty level out of range: " + difficultyLevel);
            };
        }
    }

    public static abstract class Step {
        public final int level;

        /**
         * @param level Level to reach the step (number of death)
         */
        protected Step(int level) {
            this.level = level;
        }

        /**
         * Method called when step is reached
         * @param updater Object to use when an option is edited multiple times
         * @param gameRules GameRules of the server
         */
        public abstract void reached(MinecraftServer server, GameRules gameRules, Updater updater);
        public abstract void notReached(MinecraftServer server, GameRules gameRules);
    }

    public static abstract class Modifier<T> {
        protected T value = null;

        /**
         * Update the value if needed
         * @param newValue newValue
         */
        public abstract void update(T newValue);

        /**
         * Apply modifier to player
         * @param player Player to apply the modifier
         */
        public abstract void apply(ServerPlayerEntity player);

        public T getValue() {
            return value;
        }
    }

    /**
     * Set the number of death
     * @param n number of death
     * @param silent if the update is silent
     */
    public void setNumberOfDeath(int n, boolean silent) {
        numberOfDeath = n;
        if (silent) updateDeath(UpdateType.SILENT);
        else updateDeath(UpdateType.SET);
    }

    public int getNumberOfDeath() {
        return numberOfDeath;
    }

    public void increaseDeath(MinecraftServer server) {
        numberOfDeath++;
        updateDeath(UpdateType.INCREASE);
        updateTimerTask(server);
    }

    public void updateTimerTask(MinecraftServer server) {
        if (reducerTask != null) reducerTask.cancel();
        if (numberOfDeath == 0) return;
        reducerTask = new TimerTask() {
            @Override
            public void run() {
                decreaseDeath();
                if (numberOfDeath == 0) reducerTask.cancel();
            }
        };
        timer.schedule(reducerTask, getSecondsBeforeDecreased()*1000L, getSecondsBeforeDecreased()*1000L);
        timerStart = System.currentTimeMillis() / 1000;
    }

    public void decreaseDeath() {
        // Avoids updating the difficulty when it can’t go lower.
        // Prevents for example the difficulty decrease message when killing a boss if the difficulty doesn't decrease.
        final var steps = getSteps();
        if (numberOfDeath < steps[1].level) {
            numberOfDeath = 0;
            return;
        }

        for (int i = steps.length - 1; i > 0; i--) {
            if (numberOfDeath >= steps[i].level) {
                numberOfDeath = steps[i-1].level;
                break;
            }
        }

        updateDeath(UpdateType.DECREASE);
    }

    protected void updateDeath(UpdateType updateType) {
        final var updater = new Updater();
        final var rules = server.getGameRules();

        for (final Step step : steps) {
            if (step.level >= numberOfDeath) step.reached(server, rules, updater);
            else step.notReached(server, rules);
        }

        if (Arrays.stream(steps).noneMatch(x -> x.level == numberOfDeath) && updateType != UpdateType.SET) return;
        final var difficulty = updater.getDifficulty();
        server.setDifficulty(difficulty, true);

        onUpdate(updateType, updater);
    }

    protected abstract void onUpdate(UpdateType updateType, Updater updater);

    protected abstract @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

    protected abstract int getSecondsBeforeDecreased();

    protected abstract Step[] getSteps();

    protected static String printTime(long time) {
        long hours = 0;
        if (time > 3600) {
            hours = Math.floorDiv(time, 3600);
        }
        long minutes = 0;
        if (hours != 0 || time > 60) {
            minutes = Math.floorDiv(time - hours * 3600, 60);
        }
        long seconds = (long) Math.floor(time - hours * 3600 - minutes * 60);

        StringBuilder sb = new StringBuilder();
        if (hours != 0) {
            sb.append(hours).append(" hours ");
        }
        if (minutes != 0 || hours != 0) {
            sb.append(minutes).append(" minutes ");
        }
        sb.append(seconds).append(" seconds");

        return sb.toString();
    }

    protected static void playSoundUpdate(UpdateType updateType, ServerPlayerEntity player) {
        if (updateType == UpdateType.INCREASE || updateType == UpdateType.SET) {
            player.playSoundToPlayer(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                    SoundCategory.AMBIENT,
                    1,
                    1.2f
            );
        } else if (updateType == UpdateType.DECREASE) {
            player.playSoundToPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundCategory.AMBIENT,
                    1,
                    1
            );
        }
    }
}
