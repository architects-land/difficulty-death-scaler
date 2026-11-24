package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;

@Mixin(LivingEntity.class)
public abstract class StrongerDragon {

	@ModifyVariable(
		method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
		at = @At(value = "HEAD"),
		argsOnly = true,
		name = "dmg"
	)
	protected float reduceExplosionDamage(float dmg, ServerLevel level, DamageSource source) {
		if (!((LivingEntity) (Object) this instanceof EnderDragon)) return dmg;
		final var exp = level.registryAccess()
			.lookupOrThrow(DamageTypes.EXPLOSION.registryKey())
			.getValue(DamageTypes.PLAYER_EXPLOSION);
		final var badRespawnPoint = level.registryAccess()
			.lookupOrThrow(DamageTypes.EXPLOSION.registryKey())
			.getValue(DamageTypes.BAD_RESPAWN_POINT);
		if (source.type() != exp && source.type() != badRespawnPoint) return dmg;
		final var lvl = Math.min(getGlobalDifficulty().getNumberOfDeath(), 40);
		// percentage of damage: 1 - (level-1)/40
		return (float) Math.floor(
			Math.min((1 - (float) (lvl - 1) / 40) * dmg, dmg)
		);
	}
}
