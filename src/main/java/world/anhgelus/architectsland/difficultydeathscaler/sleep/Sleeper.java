package world.anhgelus.architectsland.difficultydeathscaler.sleep;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.TimerAccess;

public class Sleeper {
    public interface On {
        void on(MinecraftServer server);
    }

    public final String description;
    public final boolean skipNight;
    public final On execStart;
    public final On execStop;

    public final static long EVENT_DURATION = 20 * 60 * 20; // is one day

    private static boolean canSleep = true;
    private static long lastSleep;

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

    public void emit(MinecraftServer server) {
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
        TimerAccess.getTimerFromOverworld(server).dds_setTimer(when * 60 * 20, () -> runStart(server));
    }

    private void runStart(MinecraftServer server) {
        // starts
        canSleep = false;
        server.getPlayerManager().broadcast(Text.of(description), false);
        execStart.on(server);
        // schedules stop
        TimerAccess.getTimerFromOverworld(server).dds_setTimer(EVENT_DURATION, () -> {
            DifficultyDeathScaler.LOGGER.info("finished");
            execStop.on(server);
            canSleep = true;
        });
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
        lastSleep = time;
        // emitting random event
        final var rules = server.getGameRules();
        if (rules == null) {
            DifficultyDeathScaler.LOGGER.warn("Impossible to get gamerules");
            return false;
        }
        final var val = Sleepers.values;
        var ev = val[Getters.RANDOM.nextInt(val.length)];
        while (!rules.getBoolean(GameRules.DO_INSOMNIA) && ev == Sleepers.PHANTOMS_NIGHTMARE) {
            ev = val[Getters.RANDOM.nextInt(val.length)];
        }
        ev.emit(server);
        return true;
    }

    public static boolean canSleep() {
        return canSleep;
    }

    public static int percentageToEmit(int level) {
        level = Math.min(level, 40);
        return (int) Math.floor(10 / (0.95 + (double) (level * level) / 200));
    }
}
