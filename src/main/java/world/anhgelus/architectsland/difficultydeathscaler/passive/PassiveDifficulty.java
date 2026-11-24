package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import world.anhgelus.architectsland.difficultydeathscaler.passive.modifier.*;

public class PassiveDifficulty {
	public static final int LEVEL_MAX = 20;
	public static boolean ENABLED = true;

	public static void onEntitySpawn(Monster entity) {
		if (!ENABLED) return;
		final var level = difficultyLevel(entity);
		if (level == 0) return;
		PassiveBurningTimeModifier.apply(entity, adjustLevelMax(level, -0.75)); // is percentage
		PassiveDamageModifier.apply(entity, adjustLevelMax(level, 0.5)); // is percentage
		PassiveHealthModifier.apply(entity, adjustLevelMax(level, 0.5)); // is percentage
		PassiveKnockbackResistanceModifier.apply(entity, adjustLevelMax(level, 0.6)); // is add
		PassiveMovementEfficiencyModifier.apply(entity, adjustLevelMax(level, 0.3)); // is add
		PassiveSpeedModifier.apply(entity, adjustLevelMax(level, 0.15)); // is percentage
		entity.setHealth(entity.getMaxHealth());
	}

	private static int difficultyLevel(Monster entity) {
		final var dis = entity.getPosition(0).distanceTo(new Vec3(0, 62, 0));
		final var timePassed = Mth.floor((double) entity.level().getGameTime() / 20000);
		return Math.min(
			Mth.floor((dis / 5000) * (dis / 1000)) +
				Mth.floorDiv(timePassed * timePassed, 8000),
			LEVEL_MAX
		);
	}

	private static double adjustLevelMax(int level, int max) {
		return adjustLevelMax(level, (double) max);
	}

	private static double adjustLevelMax(int level, double max) {
		return (level * max) / LEVEL_MAX;
	}
}
