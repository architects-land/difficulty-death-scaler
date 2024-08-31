package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Difficulty {
    public static int SECONDS_BEFORE_DECREASED;

    protected final Timer timer = new Timer();
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
        public static Identifier MODIFIER_ID;

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

        onUpdate(updater);
    }

    protected abstract void onUpdate(Updater updater);

    protected abstract @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

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
