package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class DifficultyManager extends DifficultyTimer {
    protected final Timer timer = new Timer();
    private TimerTask reducerTask;

    protected final long secondsBeforeDecreased;

    protected final StepPair[] steps;
    protected final MinecraftServer server;

    protected int numberOfDeath;

    protected DifficultyManager(MinecraftServer server, StepPair[] steps, long secondsBeforeDecreased) {
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
         * Other kind of update
         * <p>
         * Weird stuff can happen if you use this
         */
        OTHER
    }

    public static final class StepPair extends Pair<Integer, Step> {
        public StepPair(Integer level, Step reached) {
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

        private final Map<Class<? extends Modifier>, Modifier> map = new HashMap<>();

        public static final Map<net.minecraft.world.Difficulty, Integer> DIFFICULTY_LEVEL = Map.of(
                net.minecraft.world.Difficulty.PEACEFUL, 0,
                net.minecraft.world.Difficulty.EASY, 1,
                net.minecraft.world.Difficulty.NORMAL, 2,
                net.minecraft.world.Difficulty.HARD, 3
        );

        public void updateDifficulty(int level) {
            if (level > difficultyLevel) difficultyLevel = level;
        }

        public Modifier getModifier(Class<? extends Modifier> clazz) {
            Modifier val = map.get(clazz);
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

        public List<Modifier> getModifiers() {
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

    @FunctionalInterface
    public interface Step {
        void reached(MinecraftServer server, GameRules gamerules, Updater updater);
    }

    public static abstract class Modifier {
        protected double value = 0;
        protected final Identifier id;
        protected final RegistryEntry<EntityAttribute> attribute;
        protected final EntityAttributeModifier.Operation operation;

        protected Modifier(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation) {
            this.id = id;
            this.attribute = attribute;
            this.operation = operation;
        }

        /**
         * Update the value if needed
         * @param newValue newValue
         */
        public abstract void update(double newValue);

        /**
         * Apply modifier to player
         * @param player Player to apply the modifier
         */
        public void apply(ServerPlayerEntity player) {
            apply(id, attribute, operation, player, value);
        }

        protected static void apply(
                Identifier id,
                RegistryEntry<EntityAttribute> attribute,
                EntityAttributeModifier.Operation operation,
                ServerPlayerEntity player,
                double value
        ) {
            final var attr = player.getAttributeInstance(attribute);
            if (attr == null) return;

            attr.removeModifier(id);
            if (value == 0) return;

            final var playerHealthModifier = new EntityAttributeModifier(
                    id, value, operation
            );
            attr.addPersistentModifier(playerHealthModifier);
        }

        public double getValue() {
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
        updateTimerTask();
    }

    public int getNumberOfDeath() {
        return numberOfDeath;
    }

    public void increaseDeath() {
        increaseDeath(false);
    }

    public void increaseDeath(boolean other) {
        numberOfDeath++;
        if (other) updateDeath(UpdateType.OTHER);
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
                if (numberOfDeath == 0) reducerTask.cancel();
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
        final var updater = new Updater();
        final var rules = server.getGameRules();

        for (final StepPair step : steps) {
            if (step.level() <= numberOfDeath) step.reached(server, rules, updater);
            else break;
        }

        if (Arrays.stream(steps).noneMatch(x -> x.level() == numberOfDeath) && updateType != UpdateType.SET) return;
        final var difficulty = updater.getDifficulty();
        server.setDifficulty(difficulty, true);

        onUpdate(updateType, updater);
    }

    public void stopAutomaticDecrease() {
        if (reducerTask != null) reducerTask.cancel();
    }

    public String getDifficultyUpdate(net.minecraft.world.Difficulty difficulty) {
        return generateDifficultyUpdate(null, difficulty);
    }

    protected abstract void onUpdate(UpdateType updateType, Updater updater);

    /**
     * Generate difficulty update
     * @param updateType Type of update (if null, it's a get and not an update)
     * @param difficulty Difficulty of the game
     * @return Message to print
     */
    protected abstract @NotNull String generateDifficultyUpdate(@Nullable UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

    public abstract void applyModifiers(ServerPlayerEntity player);

    public abstract void save();

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

    protected String generateFooterUpdate(StepPair[] steps, UpdateType updateType) {
        if (numberOfDeath < steps[1].level()) {
            return "The difficulty cannot get lower. Congratulations!\n§8=============================================§r";
        }
        final var sb = new StringBuilder();

        if (updateType == UpdateType.DECREASE) {
            sb.append("You only need to survive for §6")
                .append(printTime(secondsBeforeDecreased))
                .append("§r to make the difficulty decrease again.");
        } else if (updateType != UpdateType.INCREASE) {
            sb.append("You only need to survive for §6")
                .append(printTime(secondsBeforeDecreased - System.currentTimeMillis() / 1000 + timerStart))
                .append("§r to make the difficulty decrease.");
        } else if (numberOfDeath < steps[2].level()) {
            sb.append("You were on the lowest difficulty for §6")
                .append(printTime(System.currentTimeMillis() / 1000 - timerStart))
                .append("§r, but you had to die and ruin everything, didn't you ?");
        } else {
            sb.append("If no one died for §6")
                .append(printTime(secondsBeforeDecreased - System.currentTimeMillis() / 1000 + timerStart))
                .append("§r, then the difficulty would’ve decreased... But you chose your fate.");
        }
        sb.append("\n§8=============================================§r");
        return sb.toString();
    }

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
