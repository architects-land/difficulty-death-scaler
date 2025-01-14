package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class DifficultyManager extends DifficultyTimer {
    private TimerTask reducerTask;

    protected final long secondsBeforeDecreased;

    protected final Step[] steps;
    protected final MinecraftServer server;

    protected int numberOfDeath;

    protected DifficultyManager(MinecraftServer server, Step[] steps, long secondsBeforeDecreased) {
        timer = new Timer();
        this.server = server;
        this.steps = steps;
        numberOfDeath = 0;
        this.secondsBeforeDecreased = secondsBeforeDecreased;
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
        SILENT,
        /**
         * Increase not linked with death
         */
        AUTOMATIC_INCREASE
    }

    @FunctionalInterface
    public interface Reached {
        void reached(MinecraftServer server, GameRules gamerules, Updater updater);
    }

    public static final class Step extends Pair<Integer, Reached> {
        public Step(Integer level, Reached reached) {
            super(level, reached);
        }

        public int level() {
            return getLeft();
        }

        public void reached(MinecraftServer server, GameRules rules, Updater updater) {
            getRight().reached(server, rules, updater);
        }
    }

    public static final class Updater {
        private int difficultyLevel = 1;

        private final Map<Class<? extends Modifier<?>>, Modifier<?>> map = new HashMap<>();

        public void updateDifficulty(int level) {
            if (level > difficultyLevel) {
                difficultyLevel = level;
            }
        }

        public Modifier<?> getModifier(Class<? extends Modifier<?>> clazz) {
            var val = map.get(clazz);
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

    /**
     * Set the number of death
     * @param n number of death
     * @param silent if the update is silent
     */
    public void setNumberOfDeath(int n, boolean silent) {
        numberOfDeath = n;
        if (silent) updateDeath(UpdateType.SILENT);
        else updateDeath(UpdateType.SET);
        updateTimerTask();
    }

    public int getNumberOfDeath() {
        return numberOfDeath;
    }

    public void increaseDeath() {
        increaseDeath(false);
    }

    public void increaseDeath(boolean automaticIncrease) {
        numberOfDeath++;
        if (automaticIncrease) updateDeath(UpdateType.AUTOMATIC_INCREASE);
        else updateDeath(UpdateType.INCREASE);
        updateTimerTask();
    }

    public void updateTimerTask() {
        if (reducerTask != null) reducerTask.cancel();
        if (numberOfDeath == 0) return;
        final var task = new TimerTask() {
            @Override
            public void run() {
                decreaseDeath();
                timerStart = System.currentTimeMillis() / 1000;
                if (numberOfDeath == 0) {
                    reducerTask.cancel();
                    timerStart = -1;
                }
            }
        };
        timerStart = System.currentTimeMillis() / 1000;
        executeTask(task, reducerTask, secondsBeforeDecreased);
        reducerTask = task;
    }

    public void decreaseDeath() {
        // Avoids updating the difficulty when it can’t go lower.
        // Prevents for example the difficulty decrease message when killing a boss if the difficulty doesn't decrease.
        if (numberOfDeath < steps[1].level()) {
            numberOfDeath = 0;
            return;
        }

        for (int i = steps.length - 1; i > 0; i--) {
            if (numberOfDeath >= steps[i].level()) {
                numberOfDeath = steps[i-1].level();
                break;
            }
        }

        updateDeath(UpdateType.DECREASE);
    }

    protected void updateDeath(UpdateType updateType) {
        final var updater = getUpdater();

        if (updateType != UpdateType.DECREASE) onDeath(updateType, updater);

        if (Arrays.stream(steps).noneMatch(x -> x.level() == numberOfDeath) && updateType != UpdateType.SET) return;

        getUpdatedSteps(updater);

        onUpdate(updateType, updater);
    }

    protected Updater getUpdater() {
        final var updater = new Updater();
        getUpdatedSteps(updater);
        return updater;
    }

    protected void getUpdatedSteps(Updater updater) {
        final var rules = server.getGameRules();

        var i = 0;
        var valid = true;
        while (i < steps.length && valid) {
            if (steps[i].level() <= numberOfDeath) steps[i].reached(server, rules, updater);
            else valid = false;
            i++;
        }
    }

    public void stopAutomaticDecrease() {
        if (reducerTask != null) reducerTask.cancel();
    }

    public String getDifficultyUpdate(net.minecraft.world.Difficulty difficulty) {
        return generateDifficultyUpdate(null, difficulty);
    }

    protected abstract void onUpdate(UpdateType updateType, Updater updater);

    protected void onDeath(UpdateType updateType, Updater updater) {}

    /**
     * Generate difficulty update
     * @param updateType Type of update (if null, it's a get and not an update)
     * @param difficulty Difficulty of the game
     * @return Message to print
     */
    protected abstract @NotNull String generateDifficultyUpdate(@Nullable UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

    public abstract void applyModifiers(ServerPlayerEntity player);

    public abstract void save();

    protected List<Modifier<?>> getModifiers(int level) {
        final var updater = new Updater();
        for (final Step step : steps) {
            if (step.level() <= level) step.reached(server, server.getGameRules(), updater);
            else break;
        }
        return updater.getModifiers();
    }

    protected void updateModifiersValue(Updater updater) {
        updateModifiersValue(updater.getModifiers());
    }

    protected abstract void updateModifiersValue(List<Modifier<?>> modifiers);

    protected String generateHeaderUpdate(@Nullable UpdateType updateType) {
        final var sb = new StringBuilder();
        if (updateType == null) sb.append( "§8============== §rCurrent difficulty: §8==============§r");
        else {
            switch (updateType) {
                case INCREASE -> sb.append("§8============== §rDifficulty increase! §8==============§r");
                case DECREASE -> sb.append("§8============== §rDifficulty decrease! §8==============§r");
                case SET -> sb.append("§8=============== §rDifficulty change! §8===============§r");
                default -> sb.append("§8============== §rCurrent difficulty: §8==============§r");
            }
        }
        sb.append("\n");
        sb.append("Number of death: §d").append(numberOfDeath).append("§r\n");
        return sb.toString();
    }

    protected String generateFooterUpdate(Step[] steps, String beginning, UpdateType updateType) {
        if (numberOfDeath < steps[1].level()) {
            return "The difficulty cannot get lower. Congratulations!\n§8=============================================§r";
        }
        final var sb = new StringBuilder();

        if (updateType == UpdateType.DECREASE) {
            sb.append("You only need to survive for §6")
                .append(formatSeconds(secondsBeforeDecreased))
                .append("§r to make the difficulty decrease again.");
        } else if (updateType == UpdateType.AUTOMATIC_INCREASE) {
            sb.append("The difficulty is increasing automatically!");
        } else if (updateType != UpdateType.INCREASE) {
            sb.append("You only need to survive for §6")
                .append(formatSeconds(secondsBeforeDecreased - System.currentTimeMillis() / 1000 + timerStart))
                .append("§r to make the difficulty decrease.");
        } else if (numberOfDeath < steps[2].level()) {
            sb.append("You were on the lowest difficulty for §6")
                .append(formatSeconds(System.currentTimeMillis() / 1000 - timerStart))
                .append("§r, but you had to die and ruin everything, hadn't you?");
        } else {
            sb.append("If ").append(beginning).append(" for §6")
                .append(formatSeconds(secondsBeforeDecreased - System.currentTimeMillis() / 1000 + timerStart))
                .append("§r, then the difficulty would’ve decreased... But you chose your fate.");
        }
        sb.append("\n§8=============================================§r");
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

    @Override
    public abstract String toString();
}
