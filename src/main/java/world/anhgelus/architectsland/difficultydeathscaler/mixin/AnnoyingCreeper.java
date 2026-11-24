package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;

public abstract class AnnoyingCreeper {
	@Mixin(SwellGoal.class)
	private abstract static class IgniteGoal {
		@Shadow
		@Final
		private net.minecraft.world.entity.monster.Creeper creeper;

		@Shadow
		private LivingEntity target;

		@ModifyReturnValue(at = @At("RETURN"), method = "canUse()Z")
		public boolean canStart(boolean original) {
			if (!GlobalDifficultyManager.areCreepersBetter()) return original;
			final var livingEntity = creeper.getTarget();
			return creeper.getSwellDir() > 0 || livingEntity != null && creeper.distanceToSqr(livingEntity) < 6.0;
		}

		@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
		public void tick(CallbackInfo ci) {
			if (!GlobalDifficultyManager.areCreepersBetter()) return;
			ci.cancel();
			if (this.target == null) {
				this.creeper.setSwellDir(-1);
				return;
			}
			if (this.creeper.isPowered()) {
				if (this.creeper.distanceToSqr(this.target) > 49.0 || !this.creeper.getSensing().hasLineOfSight(this.target)) {
					this.creeper.setSwellDir(-1);
				} else {
					creeper.setSwellDir(1);
				}
				return;
			}
			if (this.creeper.distanceToSqr(this.target) > 20.0 ||
				(!this.creeper.getSensing().hasLineOfSight(this.target) && this.creeper.distanceToSqr(this.target) > 49.0)
			) {
				this.creeper.setSwellDir(-1);
				return;
			}
			creeper.setSwellDir(2);
		}
	}

	@Mixin(net.minecraft.world.entity.monster.Creeper.class)
	private abstract static class Creeper extends Monster {
		@Shadow
		@Final
		private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

		protected Creeper(EntityType<? extends Monster> entityType, Level world) {
			super(entityType, world);
		}

		@Inject(at = @At("RETURN"), method = "<init>")
		protected void init(EntityType<? extends Monster> entityType, Level world, CallbackInfo ci) {
			final var difficulty = getGlobalDifficulty();
			if (difficulty == null) return;
			if (difficulty.getNumberOfDeath() < 21) return;
			MobUtils.customSpawn(getRandom(), 2 * (difficulty.getNumberOfDeath() - 21), 40, () -> {
				this.entityData.set(DATA_IS_POWERED, true);
				return null;
			});
		}

		@Inject(at = @At("RETURN"), method = "registerGoals")
		protected void initGoals(CallbackInfo ci) {
			if (!GlobalDifficultyManager.areCreepersBetter()) return;
			for (Goal g : targetSelector.getAvailableGoals()) {
				if (g instanceof NearestAttackableTargetGoal) {
					targetSelector.removeGoal(g);
				}
			}
			targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
		}
	}
}
