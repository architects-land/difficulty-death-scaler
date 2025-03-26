package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import world.anhgelus.architectsland.difficultydeathscaler.passive.modifier.*;

public class PassiveDifficulty {
    private final MinecraftServer server;

    public PassiveDifficulty(MinecraftServer server) {
        this.server = server;
    }

    public void onEntitySpawn(HostileEntity entity) {
        PassiveArmorModifier.apply(entity, 0); // is add
        PassiveBurningTimeModifier.apply(entity, 0); // is add
        PassiveDamageModifier.apply(entity, 0); // is percentage
        PassiveHealthModifier.apply(entity, 0); // is percentage
        PassiveKnockbackResistanceModifier.apply(entity, 0); // is add
        PassiveMovementEfficiencyModifier.apply(entity, 0); // is add
        PassiveSpeedModifier.apply(entity, 0); // is percentage
    }
}
