package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import world.anhgelus.architectsland.difficultydeathscaler.passive.modifier.*;

public class PassiveDifficulty {
    public static final int LEVEL_MAX = 20;

    public static void onEntitySpawn(HostileEntity entity) {
        final var level = difficultyLevel(entity);
        PassiveArmorModifier.apply(entity, adjustLevelMax(level, 15)); // is add
        PassiveBurningTimeModifier.apply(entity, adjustLevelMax(0, -0.75)); // is percentage
        PassiveDamageModifier.apply(entity, adjustLevelMax(level, 1)); // is percentage
        PassiveHealthModifier.apply(entity, adjustLevelMax(level, 1)); // is percentage
        PassiveKnockbackResistanceModifier.apply(entity, adjustLevelMax(level, 0.6)); // is add
        PassiveMovementEfficiencyModifier.apply(entity, adjustLevelMax(level, 0.5)); // is add
        PassiveSpeedModifier.apply(entity, adjustLevelMax(level, 0.3)); // is percentage
    }

    private static int difficultyLevel(HostileEntity entity) {
        final var dis = entity.getPos().distanceTo(new Vec3d(0, 0, 62));
        final var timePassed = MathHelper.floor((double) entity.getWorld().getTime() / 20000);
        return Math.min(
                MathHelper.floor((dis / 10000) * (dis / 1000)) +
                        MathHelper.floorDiv(timePassed * timePassed, 1000),
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
