package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import world.anhgelus.architectsland.difficultydeathscaler.passive.modifier.*;

public class PassiveDifficulty {
    public static final int LEVEL_MAX = 20;
    public static boolean ENABLED = true;

    public static void onEntitySpawn(HostileEntity entity) {
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

    private static int difficultyLevel(HostileEntity entity) {
        final var dis = entity.getPos().distanceTo(new Vec3d(0, 62, 0));
        final var timePassed = MathHelper.floor((double) entity.getWorld().getTime() / 20000);
        return Math.min(
                MathHelper.floor((dis / 5000) * (dis / 1000)) +
                        MathHelper.floorDiv(timePassed * timePassed, 8000),
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
