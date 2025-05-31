package world.anhgelus.architectsland.difficultydeathscaler.sleep;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.GameRules;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

public class Sleepers {
    public static final Sleeper LONG_NIGHT = new Sleeper("Polar night.", server -> {
        final var rules = server.getGameRules();
        if (rules == null) return;
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
    }, server -> {
        final var rules = server.getGameRules();
        if (rules == null) return;
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
    }, false);

    public static final Sleeper BRUTAL_HELL = new Sleeper("Wendy, I'm home.", server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST += 32; // for day 32
    }, server -> {
        GlobalDifficultyManager.PIGLIN_BRUTES_BOOST -= 32;
    });

    public static final Sleeper PHANTOMS_NIGHTMARE = new Sleeper("It never ends.", server -> {
        MobUtils.PHANTOMS_NIGHTMARE = true;
    }, server -> {
        MobUtils.PHANTOMS_NIGHTMARE = false;
    });

    public static final Sleeper RUN = new Sleeper("Get the fuck out! They are invisible!", server -> {
        final var since = System.currentTimeMillis() / 50;
        MobUtils.HOSTILE_MOBS_BURN = false;
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
            final var now = System.currentTimeMillis() / 50;

            RegistryEntry<StatusEffect> effect;
            if (Getters.RANDOM.nextFloat() * 100 > 90) effect = StatusEffects.RESISTANCE;
            else effect = StatusEffects.INVISIBILITY;

            final var diff = (int) (Sleeper.EVENT_DURATION - now + since);
            if (diff < 0) throw new IllegalStateException("Difference in event RUN is below zero");

            living.addStatusEffect(new StatusEffectInstance(effect, diff, 2, false, false));
        };
    }, server -> {
        GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
        };
        MobUtils.HOSTILE_MOBS_BURN = true;
    });

    public static final Sleeper[] values = {LONG_NIGHT, PHANTOMS_NIGHTMARE, BRUTAL_HELL, RUN};
}
