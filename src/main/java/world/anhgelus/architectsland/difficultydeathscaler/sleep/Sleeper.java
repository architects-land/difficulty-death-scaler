package world.anhgelus.architectsland.difficultydeathscaler.sleep;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

public class Sleeper {
    public final static long EVENT_DURATION = 20 * 60 * 20; // is one day
    public static boolean ENABLED = true;
    private static boolean canSleep = true;
    private static long lastSleep;
    public final String description;
    public final boolean skipNight;
    public final On execStart;
    public final On execStop;

    public Sleeper(String description, On execStart, On execStop, boolean skipNight) {
        this.description = description;
        this.execStart = execStart;
        this.execStop = execStop;
        this.skipNight = skipNight;
    }

    public Sleeper(String description, On execStart, On execStop) {
        this.description = description;
        this.skipNight = true;
        this.execStart = execStart;
        this.execStop = execStop;
    }

    public static boolean tryEmitNewEvent(MinecraftServer server) {
        // prevent emitting events during the same night
        final var world = server.getWorld(World.OVERWORLD);
        if (world == null) {
            DifficultyDeathScaler.LOGGER.warn("Impossible to get the overworld");
            return false;
        }
        final var time = world.getTime();
        if (time - lastSleep < 10 * 60 * 20) return false;
        // emitting random event
        final var rules = server.getGameRules();
        if (rules == null) {
            DifficultyDeathScaler.LOGGER.warn("Impossible to get gamerules");
            return false;
        }
        lastSleep = time;
        final var val = Sleepers.values;
        var ev = val[Getters.RANDOM.nextInt(val.length)];
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
        if (!ENABLED) throw new IllegalStateException("REDACTED is not enabled");
        if (!skipNight) {
            server.getPlayerManager()
                    .getPlayerList()
                    .stream()
                    .filter(LivingEntity::isSleeping)
                    .forEach(PlayerEntity::wakeUp);
            runStart(server);
            return;
        }
        final long when = (long) Math.floor(3 * Getters.RANDOM.nextFloat() + 2); // between 2 and 5
        TimerAccess.getTimerFromOverworld(server).dds_runTask(new TickTask(() -> runStart(server), when * 60 * 20));
    }

    private void runStart(MinecraftServer server) {
        // starts
        canSleep = false;
        server.getPlayerManager().broadcast(Text.of(description), false);
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
