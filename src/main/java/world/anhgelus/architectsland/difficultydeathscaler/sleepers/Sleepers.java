package world.anhgelus.architectsland.difficultydeathscaler.sleepers;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import java.util.Timer;
import java.util.TimerTask;

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
    BRUTAL_HELL("Axes are wainting for you.", server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST += 30; // is like day 28
    }, server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST -= 30;
    }),
    PHANTOMS_NIGHTMARE("It never ends.", server -> {}, server -> {}),
    RUN("Don't fight, run!", server -> {
        final var since = System.currentTimeMillis() / 50;
        MobUtils.HOSTILE_MOBS_BURN = false;
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
            final var now = System.currentTimeMillis() / 50;

            RegistryEntry<StatusEffect> effect;
            if (Getters.RANDOM.nextFloat() * 100 > 90) effect = StatusEffects.RESISTANCE;
            else effect = StatusEffects.INVISIBILITY;

            final var diff = (int) (20*60*20 - now + since);
            if (diff < 0) throw new IllegalStateException("Difference in event RUN is below zero");

            living.addStatusEffect(new StatusEffectInstance(effect, diff, 2, false, false));
        };
    }, server -> {
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {};
        MobUtils.HOSTILE_MOBS_BURN = true;
    }),
    ANVIL_RAIN("It's raining.", server -> {}, server -> {});

    public interface On {
        void on(MinecraftServer server);
    }

    public final String description;
    public final boolean skipNight;
    public final On execStart;
    public final On execStop;

    private static final Timer timer = new Timer();
    private static boolean canSleep;

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
            //TODO: handle skipping the night
            runStart(server);
            return;
        }
        final long when = (long) Math.floor(3*Getters.RANDOM.nextFloat()+2); // between 2 and 5
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runStart(server);
            }
        }, when*60*1000);
    }

    private void runStart(MinecraftServer server) {
        // starts
        canSleep = skipNight;
        server.getPlayerManager().broadcast(Text.of(description), false);
        execStart.on(server);
        // schedule stop 20 minutes later
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                execStop.on(server);
                canSleep = true;
            }
        }, 20*60*1000);
    }

    public static boolean canSleep() {
        return canSleep;
    }

    public static int percentageToEmit(int level) {
        return (int) Math.floor(10 / (0.95 + (double) (level * level) /200));
    }

    public static void stop() {
        timer.cancel();
    }
}
