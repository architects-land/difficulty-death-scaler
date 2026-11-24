package world.anhgelus.architectsland.difficultydeathscaler.sleep;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

public record Sleeper(String description, boolean skipNight, On execStart, On execStop) {
	public final static long EVENT_DURATION = 20 * 60 * 20; // is one day
	private static boolean canSleep = true;
	private static long lastSleep;

	public Sleeper(String description, On execStart, On execStop, boolean skipNight) {
		this(description, skipNight, execStart, execStop);
	}

	public Sleeper(String description, On execStart, On execStop) {
		this(description, true, execStart, execStop);
	}

	public static boolean tryEmitNewEvent(MinecraftServer server) {
		// prevent emitting events during the same night
		final var world = server.overworld();
		final var time = world.getGameTime();
		if (time - lastSleep < 10 * 60 * 20) return false;
		// emitting random event
		final var rules = server.getGameRules();
		if (rules == null) {
			DifficultyDeathScaler.LOGGER.warn("Impossible to get gamerules");
			return false;
		}
		lastSleep = time;
		final var val = Sleepers.values;
		var ev = val[server.overworld().getRandom().nextInt(val.length)];
		ev.emit(server);
		return true;
	}

	public static boolean canSleep() {
		return canSleep;
	}

	public static int percentageToEmit(int level) {
		level = Math.min(level, 40);
		return 2 * (int) Math.floor(10 / (0.95 + (double) (level * level) / 200));
	}

	public void emit(MinecraftServer server) {
		if (!server.getGameRules().get(DifficultyDeathScaler.ENABLE_REDACTED))
			throw new IllegalStateException("REDACTED is not enabled");
		if (!skipNight) {
			server.getPlayerList()
				.getPlayers()
				.stream()
				.filter(LivingEntity::isSleeping)
				.forEach(Player::stopSleeping);
			runStart(server);
			return;
		}
		final long when = (long) Math.floor(3 * server.overworld().getRandom().nextFloat() + 2); // between 2 and 5
		TimerAccess.getTimerFromOverworld(server).dds_runTask(new TickTask(() -> runStart(server), when * 60 * 20));
	}

	private void runStart(MinecraftServer server) {
		// starts
		canSleep = false;
		server.getPlayerList().broadcastSystemMessage(Component.nullToEmpty(description), false);
		execStart.on(server);
		// schedules stop
		TimerAccess.getTimerFromOverworld(server).dds_runTask(new TickTask(() -> {
			execStop.on(server);
			canSleep = true;
		}, EVENT_DURATION));
	}

	public interface On {
		void on(MinecraftServer server);
	}
}
