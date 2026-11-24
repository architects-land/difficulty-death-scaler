package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.AdvancementProvider;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Constants;

import java.util.Arrays;
import java.util.List;

public abstract class DifficultyManager<T extends DifficultyState<T>> extends DifficultyTimer {
	protected final long secondsBeforeDecreased;
	protected final Step[] steps;
	protected final MinecraftServer server;
	protected TickTask reducerTask;

	protected TimerAccess.TickTask secondsLowerDifficultyCounterTask;

	protected T data;

	protected DifficultyManager(MinecraftServer server,
	                            Step[] steps,
	                            long secondsBeforeDecreased) {
		timer = TimerAccess.getTimerFromOverworld(server);
		this.server = server;
		this.steps = steps;
		this.secondsBeforeDecreased = secondsBeforeDecreased;
	}

	protected static void playSoundUpdate(UpdateType updateType, ServerPlayer player) {
		if (updateType == UpdateType.INCREASE || updateType == UpdateType.SET) {
			player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1, 1.2f);
		} else if (updateType == UpdateType.DECREASE) {
			player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
		}
	}

	public int getNumberOfDeath() {
		return data.deaths();
	}

	/**
	 * Set the number of death
	 *
	 * @param n number of death
	 */
	public void setNumberOfDeath(int n) {
		final var before = data.deaths();
		data = data.deaths(n);
		// this order is required because without it will break the counter
		// BUT this provokes a bad timer to be displayed after restart (not really important)
		updateDeath(UpdateType.SET, before);
		updateTimerTask();
	}

	public long getTimeBeforeReduce() {
		return reducerTask != null && reducerTask.isRunning()
			? secondsBeforeDecreased * 20 - reducerTask.getTickingBeforeRun()
			: 0;
	}

	public int getTotalOfDeath() {
		return data.deathsTotal();
	}

	public void increaseDeath() {
		increaseDeath(false);
	}

	public void increaseDeath(boolean automaticIncrease) {
		final var before = data.deaths();
		data = data.increaseDeath();
		if (automaticIncrease) {
			server.getPlayerList().getPlayers().forEach(p -> {
				ModCriteria.ARBITRARY.trigger(p, AdvancementProvider.DIFFICULTY_INCREASE);
			});
			updateDeath(UpdateType.AUTOMATIC_INCREASE, before);
		} else updateDeath(UpdateType.INCREASE, before);
		updateTimerTask();
	}

	public void updateTimerTask() {
		if (reducerTask != null && reducerTask.isRunning()) reducerTask.cancel();
		if (data.deaths() == 0) {
			data = data.secondsLowerDifficulty(0);
			secondsLowerDifficultyCounterTask = new TickTask(() -> {
				if (data.deaths() > 0 && secondsLowerDifficultyCounterTask.isRunning())
					secondsLowerDifficultyCounterTask.cancel();
				data.increaseSecondsLowerDifficulty();
			}, 20, 20);
			timer.dds_runTask(secondsLowerDifficultyCounterTask);
			return;
		}
		reducerTask = executeTask(() -> {
			decreaseDeath();
			if (data.deaths() == 0) reducerTask.cancel();
		}, reducerTask, secondsBeforeDecreased);
	}

	public T save() {
		return data.timeBeforeReduce(getTimeBeforeReduce());
	}

	public void decreaseDeath() {
		// Avoids updating the difficulty when it can’t go lower.
		// Prevents for example the difficulty decrease message when killing a boss if the
		// difficulty doesn't decrease.
		if (data.deaths() < steps[1].level()) {
			data = data.deaths(0);
			updateTimerTask();
			return;
		}

		final var before = data.deaths();

		for (int i = steps.length - 1; i > 0; i--) {
			if (before >= steps[i].level()) {
				data = data.deaths(steps[i - 1].level());
				break;
			}
		}

		updateDeath(UpdateType.DECREASE, before);
	}

	protected void updateDeath(UpdateType updateType, int before) {
		final var updater = getUpdater();

		if (updateType == UpdateType.INCREASE)
			onDeath(updateType, updater); // increase is always linked with the death

		if (Arrays.stream(steps).noneMatch(x -> x.level() == data.deaths()) &&
			updateType != UpdateType.SET)
			return;

		onUpdate(updateType, updater, before);
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
			if (steps[i].level() <= data.deaths()) steps[i].reached(server, rules, updater);
			else valid = false;
			i++;
		}
	}

	public void stopAutomaticDecrease() {
		if (reducerTask != null) reducerTask.cancel();
	}

	public Component getDifficultyUpdate(net.minecraft.world.Difficulty difficulty) {
		return generateDifficultyUpdate(null, difficulty);
	}

	protected abstract void onUpdate(UpdateType updateType, DifficultyUpdater updater, int before);

	protected void onDeath(UpdateType updateType, DifficultyUpdater updater) {
	}

	/**
	 * Generate difficulty update
	 *
	 * @param updateType Type of update (if null, it's a get and not an update)
	 * @param difficulty Difficulty of the game
	 * @return Message to print
	 */
	protected abstract @NotNull Component generateDifficultyUpdate(@Nullable UpdateType updateType, @Nullable net.minecraft.world.Difficulty difficulty);

	public abstract void applyModifiers(ServerPlayer player);

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

	protected Component generateHeaderUpdate(@Nullable UpdateType updateType) {
		final var txt = Component.empty();
		txt.append(Component.literal("============== ").withStyle(Constants.COLOR_SEPARATOR));
		if (updateType == null) txt.append(Component.literal("Current difficulty:"));
		else {
			switch (updateType) {
				case INCREASE -> txt.append("Difficulty increase!");
				case DECREASE -> txt.append("Difficulty decrease!");
				case SET -> txt.append("Difficulty change!");
				default -> txt.append("Current difficulty:");
			}
		}
		txt.append(Component.literal(" ==============").withStyle(Constants.COLOR_SEPARATOR));
		txt.append("\n");
		txt.append("Death step: ")
			.append(Component.literal(String.format("%d", data.deaths())).withStyle(Constants.COLOR_ACCENT))
			.append("\n");
		return txt;
	}

	protected Component generateFooterUpdate(Step[] steps, String beginning, UpdateType updateType) {
		final var txt = Component.empty();
		if (data.deaths() < steps[1].level()) {
			return txt.append(Component.literal("The difficulty cannot get lower. Congratulations!\n").withStyle(Constants.COLOR_OK))
				.append(Component.literal("=============================================").withStyle(Constants.COLOR_SEPARATOR));
		}

		if (updateType == UpdateType.DECREASE) {
			txt.append("You only need to survive for ")
				.append(Component.literal(formatSeconds(secondsBeforeDecreased)).withStyle(Constants.COLOR_TIME))
				.append(" to make the difficulty decrease again.");
		} else if (updateType == UpdateType.AUTOMATIC_INCREASE) {
			txt.append("The difficulty is increasing automatically!");
		} else if (updateType != UpdateType.INCREASE) {
			txt.append("You only need to survive for ");
			final var t = Component.empty().withStyle(Constants.COLOR_TIME);
			if (updateType == UpdateType.SET) t.append(formatSeconds(secondsBeforeDecreased));
			else t.append(formatSecondsBeforeRun(reducerTask));
			txt.append(t);
			txt.append(" to make the difficulty decrease.");
		} else if (data.deaths() == steps[1].level()) {
			txt.append("You were on the lowest difficulty for ")
				.append(Component.literal(formatSeconds(data.secondsLowerDifficulty())).withStyle(Constants.COLOR_TIME))
				.append(", but you had to die and ruin everything, hadn't you?");
		} else {
			txt.append("If ").append(beginning).append(" for")
				.append(Component.literal(formatSecondsBeforeRun(reducerTask)).withStyle(Constants.COLOR_TIME))
				.append(", then the difficulty would’ve decreased... But you chose your fate.");
		}
		txt.append("\n");
		txt.append(Component.literal("=============================================").withStyle(Constants.COLOR_SEPARATOR));
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
			return getFirst();
		}

		public void reached(MinecraftServer server, GameRules rules, DifficultyUpdater updater) {
			getSecond().reached(server, rules, updater);
		}
	}
}
