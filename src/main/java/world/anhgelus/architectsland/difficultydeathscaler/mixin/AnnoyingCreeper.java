package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

public abstract class AnnoyingCreeper {
    @Mixin(CreeperIgniteGoal.class)
    private abstract static class IgniteGoal {
        @Shadow
        @Final
        private CreeperEntity creeper;

        @Shadow
        private LivingEntity target;

        @Inject(at = @At("RETURN"), method = "canStart", cancellable = true)
        public void canStart(CallbackInfoReturnable<Boolean> cir) {
            if (!GlobalDifficultyManager.areCreepersBetter()) return;
            final var livingEntity = creeper.getTarget();
            cir.setReturnValue(creeper.getFuseSpeed() > 0 || livingEntity != null && creeper.squaredDistanceTo(livingEntity) < 6.0);
        }

        @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
        public void tick(CallbackInfo ci) {
            if (!GlobalDifficultyManager.areCreepersBetter()) return;
            ci.cancel();
            if (this.target == null) {
                this.creeper.setFuseSpeed(-1);
                return;
            }
            if (this.creeper.isCharged()) {
                if (this.creeper.squaredDistanceTo(this.target) > 49.0 || !this.creeper.getVisibilityCache().canSee(this.target)) {
                    this.creeper.setFuseSpeed(-1);
                } else {
                    creeper.setFuseSpeed(1);
                }
                return;
            }
            if (this.creeper.squaredDistanceTo(this.target) > 20.0 ||
                    (!this.creeper.getVisibilityCache().canSee(this.target) && this.creeper.squaredDistanceTo(this.target) > 49.0)
            ) {
                this.creeper.setFuseSpeed(-1);
                return;
            }
            creeper.setFuseSpeed(2);
        }
    }

    @Mixin(CreeperEntity.class)
    private abstract static class Creeper extends HostileEntity {
        protected Creeper(EntityType<? extends HostileEntity> entityType, World world) {
            super(entityType, world);
        }

        @Shadow
        @Final
        private static TrackedData<Boolean> CHARGED;

        @Inject(at = @At("RETURN"), method = "<init>")
        protected void init(EntityType<? extends HostileEntity> entityType, World world, CallbackInfo ci) {
            final var difficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get();
            if (difficulty == null) return;
            if (Math.random() * 100 < 2 * (difficulty.getNumberOfDeath() % 21))
                this.dataTracker.set(CHARGED, true);
        }

        @Inject(at = @At("RETURN"), method = "initGoals")
        protected void initGoals(CallbackInfo ci) {
            if (!GlobalDifficultyManager.areCreepersBetter()) return;
            for (Goal g : targetSelector.getGoals()) {
                if (g instanceof ActiveTargetGoal) {
                    targetSelector.remove(g);
                }
            }
            targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        }
    }
}
