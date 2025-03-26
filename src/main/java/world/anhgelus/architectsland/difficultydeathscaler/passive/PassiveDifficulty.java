package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import world.anhgelus.architectsland.difficultydeathscaler.passive.modifier.*;

public class PassiveDifficulty {
    public static void onEntitySpawn(HostileEntity entity) {
        final var level = difficultyLevel(entity);
        PassiveArmorModifier.apply(entity, 0); // is add
        PassiveBurningTimeModifier.apply(entity, 0); // is add
        PassiveDamageModifier.apply(entity, 0); // is percentage
        PassiveHealthModifier.apply(entity, 0); // is percentage
        PassiveKnockbackResistanceModifier.apply(entity, 0); // is add
        PassiveMovementEfficiencyModifier.apply(entity, 0); // is add
        PassiveSpeedModifier.apply(entity, 0); // is percentage
    }

    private static int difficultyLevel(HostileEntity entity) {
        final var dis = entity.getPos().distanceTo(new Vec3d(0, 0, 62));
        final var timePassed = MathHelper.floor((double) entity.getWorld().getTime() / 20000);
        return Math.min(
                MathHelper.floor((dis / 10000) * (dis / 1000)) +
                        MathHelper.floorDiv(timePassed * timePassed, 1000),
                20
        );
    }
}
