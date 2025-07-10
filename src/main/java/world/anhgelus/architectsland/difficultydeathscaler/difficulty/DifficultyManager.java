package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

import java.util.Arrays;
import java.util.List;

public abstract class DifficultyManager extends DifficultyTimer {
    protected final long secondsBeforeDecreased;
    protected final Step[] steps;
    protected final MinecraftServer server;
    protected TickTask reducerTask;
    protected int numberOfDeath = 0;
    protected int totalOfDeath = 0;

    protected long secondsLowerDifficulty;
    protected TimerAccess.TickTask secondsLowerDifficultyCounterTask;

    protected DifficultyManager(MinecraftServer server, Step[] steps, long secondsBeforeDecreased) {
        timer = TimerAccess.getTimerFromOverworld(server);
        this.server = server;
        this.steps = steps;
        this.secondsBeforeDecreased = secondsBeforeDecreased;
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

    public int getNumberOfDeath() {
        return numberOfDeath;
    }

    /**
     * Set the number of death
     *
     * @param n number of death
     */
    public void setNumberOfDeath(int n) {
        numberOfDeath = n;
        updateDeath(UpdateType.SET);
        updateTimerTask();
    }

    public int getTotalOfDeath() {
        return totalOfDeath;
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
        if (reducerTask != null && reducerTask.isRunning()) reducerTask.cancel();
        if (numberOfDeath == 0) {
            secondsLowerDifficulty = 0;
            secondsLowerDifficultyCounterTask = new TickTask(() -> {
                if (numberOfDeath > 0 && secondsLowerDifficultyCounterTask.isRunning())
                    secondsLowerDifficultyCounterTask.cancel();
                secondsLowerDifficulty++;
            }, 20, 20);
            timer.dds_runTask(secondsLowerDifficultyCounterTask);
            return;
        }
        reducerTask = executeTask(() -> {
            decreaseDeath();
            if (numberOfDeath == 0) reducerTask.cancel();
        }, reducerTask, secondsBeforeDecreased);
    }

    public void decreaseDeath() {
        // Avoids updating the difficulty when it can’t go lower.
        // Prevents for example the difficulty decrease message when killing a boss if the difficulty doesn't decrease.
        if (numberOfDeath < steps[1].level()) {
            numberOfDeath = 0;
            updateTimerTask();
            return;
        }

        for (int i = steps.length - 1; i > 0; i--) {
            if (numberOfDeath >= steps[i].level()) {
                numberOfDeath = steps[i - 1].level();
                break;
            }
        }

        updateDeath(UpdateType.DECREASE);
    }

    protected void updateDeath(UpdateType updateType) {
        final var updater = getUpdater();

        if (updateType == UpdateType.INCREASE) onDeath(updateType, updater); // increase is always linked with the death

        if (Arrays.stream(steps).noneMatch(x -> x.level() == numberOfDeath) && updateType != UpdateType.SET) return;

        onUpdate(updateType, updater);
    }

    protected DifficultyUpdater getUpdater() {
        final var updater = new DifficultyUpdater();
        getUpdatedSteps(updater);
        return updater;
    }

    protected void getUpdatedSteps(DifficultyUpdater updater) {
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

    public Text getDifficultyUpdate(net.minecraft.world.Difficulty difficulty) {
        return generateDifficultyUpdate(null, difficulty);
    }

    protected abstract void onUpdate(UpdateType updateType, DifficultyUpdater updater);

    protected void onDeath(UpdateType updateType, DifficultyUpdater updater) {
        totalOfDeath++;
    }

    /**
     * Generate difficulty update
     *
     * @param updateType Type of update (if null, it's a get and not an update)
     * @param difficulty Difficulty of the game
     * @return Message to print
     */
    protected abstract @NotNull Text generateDifficultyUpdate(@Nullable UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

    public abstract void applyModifiers(ServerPlayerEntity player);

    protected void load(DifficultyData data) {
        totalOfDeath = data.totalOfDeath;
        secondsLowerDifficulty = data.secondsLowerDifficulty;
        delayFirstTask(data.timeBeforeReduce);
        setNumberOfDeath(data.deaths);
    }

    protected void save(DifficultyData data) {
        data.deaths = numberOfDeath;
        data.totalOfDeath = totalOfDeath;
        data.secondsLowerDifficulty = secondsLowerDifficulty;
        if (reducerTask != null && reducerTask.isRunning()) {
            data.timeBeforeReduce = secondsBeforeDecreased * 20 - reducerTask.getTickingBeforeRun();
        } else data.timeBeforeReduce = 0;
    }

    protected List<Modifier<?>> getModifiers(int level) {
        final var updater = new DifficultyUpdater();
        for (final Step step : steps) {
            if (step.level() <= level) step.reached(server, server.getGameRules(), updater);
            else break;
        }
        return updater.getModifiers();
    }

    protected void updateModifiersValue(DifficultyUpdater updater) {
        updateModifiersValue(updater.getModifiers());
    }

    protected abstract void updateModifiersValue(List<Modifier<?>> modifiers);

    protected Text generateHeaderUpdate(@Nullable UpdateType updateType) {
        final var txt = Text.empty();
        txt.append(Text.literal("============== ").formatted(Formatting.DARK_GRAY));
        if (updateType == null) txt.append(Text.literal("Current difficulty:"));
        else {
            switch (updateType) {
                case INCREASE -> txt.append("Difficulty increase!");
                case DECREASE -> txt.append("Difficulty decrease!");
                case SET -> txt.append("Difficulty change!");
                default -> txt.append("Current difficulty:");
            }
        }
        txt.append(Text.literal(" ==============").formatted(Formatting.DARK_GRAY));
        txt.append("\n");
        txt.append("Death step: ")
                .append(Text.literal(String.format("%d", numberOfDeath)).formatted(Formatting.LIGHT_PURPLE))
                .append("\n");
        return txt;
    }

    protected Text generateFooterUpdate(Step[] steps, String beginning, UpdateType updateType) {
        final var txt = Text.empty();
        if (numberOfDeath < steps[1].level()) {
            return txt.append("The difficulty cannot get lower. Congratulations!\n")
                    .append("=============================================")
                    .formatted(Formatting.DARK_GRAY);
        }

        if (updateType == UpdateType.DECREASE) {
            txt.append("You only need to survive for ")
                    .append(Text.literal(formatSeconds(secondsBeforeDecreased)).formatted(Formatting.GOLD))
                    .append(" to make the difficulty decrease again.");
        } else if (updateType == UpdateType.AUTOMATIC_INCREASE) {
            txt.append("The difficulty is increasing automatically!");
        } else if (updateType != UpdateType.INCREASE) {
            txt.append("You only need to survive for ");
            final var t = Text.empty().formatted(Formatting.GOLD);
            if (updateType == UpdateType.SET) t.append(formatSeconds(secondsBeforeDecreased));
            else t.append(formatSecondsBeforeRun(reducerTask));
            txt.append(t);
            txt.append(" to make the difficulty decrease.");
        } else if (numberOfDeath == steps[1].level()) {
            txt.append("You were on the lowest difficulty for ")
                    .append(Text.literal(formatSeconds(secondsLowerDifficulty)).formatted(Formatting.GOLD))
                    .append(", but you had to die and ruin everything, hadn't you?");
        } else {
            txt.append("If ").append(beginning).append(" for")
                    .append(Text.literal(formatSecondsBeforeRun(reducerTask)).formatted(Formatting.GOLD))
                    .append(", then the difficulty would’ve decreased... But you chose your fate.");
        }
        txt.append("\n");
        txt.append(Text.literal("=============================================").formatted(Formatting.DARK_GRAY));
        return txt;
    }

    @Override
    public abstract String toString();

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
         * Increase not linked with death
         */
        AUTOMATIC_INCREASE
    }

    @FunctionalInterface
    public interface Reached {
        void reached(MinecraftServer server, GameRules gamerules, DifficultyUpdater updater);
    }

    public static final class Step extends Pair<Integer, Reached> {
        public Step(Integer level, Reached reached) {
            super(level, reached);
        }

        public int level() {
            return getLeft();
        }

        public void reached(MinecraftServer server, GameRules rules, DifficultyUpdater updater) {
            getRight().reached(server, rules, updater);
        }
    }
}
