package world.anhgelus.architectsland.difficultydeathscaler.sleepers;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;
import world.anhgelus.architectsland.difficultydeathscaler.utils.TimerAccess;

public enum Sleepers {
    LONG_NIGHT("Polar night.", server -> {
        final var rules = server.getGameRules();
        if (rules == null) return;
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
    }, server -> {
        final var rules = server.getGameRules();
        if (rules == null) return;
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
    }, false),
    BRUTAL_HELL("Wendy, I'm home.", server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST += 30; // is like day 28
    }, server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST -= 30;
    }),
    PHANTOMS_NIGHTMARE("It never ends.", server -> {
    }, server -> {
    }),
    RUN("Get the fuck out!", server -> {
        final var since = System.currentTimeMillis() / 50;
        MobUtils.HOSTILE_MOBS_BURN = false;
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
            final var now = System.currentTimeMillis() / 50;

            RegistryEntry<StatusEffect> effect;
            if (Getters.RANDOM.nextFloat() * 100 > 90) effect = StatusEffects.RESISTANCE;
            else effect = StatusEffects.INVISIBILITY;

            final var diff = (int) (20 * 60 * 20 - now + since);
            if (diff < 0) throw new IllegalStateException("Difference in event RUN is below zero");

            living.addStatusEffect(new StatusEffectInstance(effect, diff, 2, false, false));
        };
    }, server -> {
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
        };
        MobUtils.HOSTILE_MOBS_BURN = true;
    }),
    ANVIL_RAIN("Heavy rain.", server -> {
    }, server -> {
    });

    public interface On {
        void on(MinecraftServer server);
    }

    public final String description;
    public final boolean skipNight;
    public final On execStart;
    public final On execStop;

    private static boolean canSleep;
    private static long lastSleep;

    Sleepers(String description, On execStart, On execStop, boolean skipNight) {
        this.description = description;
        this.skipNight = skipNight;
        this.execStart = execStart;
        this.execStop = execStop;
    }

    Sleepers(String description, On execStart, On execStop) {
        this.description = description;
        this.skipNight = true;
        this.execStart = execStart;
        this.execStop = execStop;
    }

    public void emit(MinecraftServer server) {
        if (!skipNight) {
            server.getPlayerManager().getPlayerList().forEach(p -> {
                if (!p.isSleeping()) return;
                p.wakeUp();
            });
            runStart(server);
            return;
        }
        final long when = (long) Math.floor(3 * Getters.RANDOM.nextFloat() + 2); // between 2 and 5
        TimerAccess.getTimerFromOverworld(server).dds_setTimer(when * 60 * 20, () -> runStart(server));
    }

    private void runStart(MinecraftServer server) {
        // starts
        canSleep = skipNight;
        server.getPlayerManager().broadcast(Text.of(description), false);
        execStart.on(server);
        // schedule stop 20 minutes later
        TimerAccess.getTimerFromOverworld(server).dds_setTimer(20 * 60 * 20, () -> {
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
//        if (time - lastSleep < 10 * 60 * 20) return false;
        lastSleep = time;
        // emitting random event
        final var rules = server.getGameRules();
        if (rules == null) {
            DifficultyDeathScaler.LOGGER.warn("Impossible to get gamerules");
            return false;
        }
        final var val = values();
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
        return (int) Math.floor(10 / (0.95 + (double) (level * level) / 200));
    }
}
