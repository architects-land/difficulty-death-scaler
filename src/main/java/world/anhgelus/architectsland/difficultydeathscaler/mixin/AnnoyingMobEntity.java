package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;

@Mixin(Mob.class)
public abstract class AnnoyingMobEntity extends LivingEntity {
	protected AnnoyingMobEntity(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "getMaxFallDistance", cancellable = true)
	public void newSafeFallDistance(CallbackInfoReturnable<Integer> cir) {
		final var t = (Mob) (Object) this;
		if (t instanceof Skeleton && GlobalDifficultyManager.areSkeletonsBetter()) {
			cir.setReturnValue(safeFallDistance(t.getTarget()));
		} else if (t instanceof Zombie && GlobalDifficultyManager.areZombiesBetter()) {
			cir.setReturnValue(safeFallDistance(t.getTarget()));
		}
	}

	@Unique
	private int safeFallDistance(LivingEntity target) {
		return target == null ? getComfortableFallDistance(0.0F) : getComfortableFallDistance(this.getHealth() - 3.0F);
	}
}
