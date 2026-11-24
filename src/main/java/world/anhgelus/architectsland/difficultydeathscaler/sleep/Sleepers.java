package world.anhgelus.architectsland.difficultydeathscaler.sleep;

import net.minecraft.core.Holder;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.gamerules.GameRules;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;

public class Sleepers {
	public static final Sleeper LONG_NIGHT = new Sleeper("Polar night.", server -> {
		final var rules = server.getGameRules();
		GlobalDifficultyManager.POLAR_NIGHT = true;
		rules.set(GameRules.ADVANCE_TIME, false, server);
		server.setDifficulty(Difficulty.HARD, true);
	}, server -> {
		final var rules = server.getGameRules();
		GlobalDifficultyManager.POLAR_NIGHT = false;
		rules.set(GameRules.ADVANCE_TIME, true, server);
		final var difficulty = getGlobalDifficulty();
		difficulty.setNumberOfDeath(difficulty.getNumberOfDeath());
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

			Holder<MobEffect> effect;
			if (living.getRandom().nextFloat() * 100 > 90) effect = MobEffects.RESISTANCE;
			else effect = MobEffects.INVISIBILITY;

			final var diff = (int) (Sleeper.EVENT_DURATION - now + since);
			if (diff < 0) throw new IllegalStateException("Difference in event RUN is below zero");

			living.addEffect(new MobEffectInstance(effect, diff, 2, false, false));
		};
	}, server -> {
		GlobalDifficultyManager.CUSTOM_SPAWN_EFFECTS = living -> {
		};
		MobUtils.HOSTILE_MOBS_BURN = true;
	});

	public static final Sleeper[] values = {LONG_NIGHT, PHANTOMS_NIGHTMARE, BRUTAL_HELL, RUN};
}
