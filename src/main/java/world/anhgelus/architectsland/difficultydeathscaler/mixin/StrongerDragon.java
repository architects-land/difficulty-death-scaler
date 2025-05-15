package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

@Mixin(LivingEntity.class)
public abstract class StrongerDragon {

    @ModifyVariable(
            method = "applyDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)V",
            at = @At(value = "HEAD"),
            ordinal = 0,
            index = 2,
            argsOnly = true
    )
    protected float reduceExplosionDamage(float amount, ServerWorld world, DamageSource source) {
        if (!((LivingEntity) (Object) this instanceof EnderDragonEntity)) return amount;
        final var exp = world.getRegistryManager()
                .getOrThrow(DamageTypes.EXPLOSION.getRegistryRef())
                .get(DamageTypes.PLAYER_EXPLOSION);
        final var badRespawnPoint = world.getRegistryManager()
                .getOrThrow(DamageTypes.EXPLOSION.getRegistryRef())
                .get(DamageTypes.BAD_RESPAWN_POINT);
        if (source.getType() != exp && source.getType() != badRespawnPoint) return amount;
        final var level = Math.min(Getters.GLOBAL_DIFFICULTY_GETTER.get().getNumberOfDeath(), 40);
        // percentage of damage: 1 - (level-1)/40
        return (float) Math.floor(
                Math.min((1 - (float) (level - 1) / 40) * amount, amount)
        );
    }
}
