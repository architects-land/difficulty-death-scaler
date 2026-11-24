package world.anhgelus.architectsland.difficultydeathscaler.utils;

import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.concurrent.Callable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class MobUtils {
	public static boolean HOSTILE_MOBS_BURN = true;
	public static boolean PHANTOMS_NIGHTMARE = false;

	public static void commonBetterGoals(Monster e, GoalSelector targetSelector) {
		for (Goal g : targetSelector.getAvailableGoals()) {
			if (g instanceof HurtByTargetGoal) {
				targetSelector.removeGoal(g);
			}
		}
		// kill player become more important than defending itself
		targetSelector.addGoal(2, new HurtByTargetGoal(e));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(e, Player.class, true));
	}

	public static void customSpawn(RandomSource random, float proba, float max, Callable<Object> exec) {
		if (proba <= 0) return;
		final var difficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get();
		if (difficulty.getNumberOfDeath() >= 40) proba = max;
		try {
			if (random.nextFloat() * 100 < proba) exec.call();
		} catch (Exception e) {
			DifficultyDeathScaler.LOGGER.error("An error occurred while executing the custom spawn", e);
		}
	}
}
