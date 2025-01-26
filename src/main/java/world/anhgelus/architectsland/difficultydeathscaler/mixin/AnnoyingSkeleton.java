package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AnnoyingSkeleton extends HostileEntity {
    protected AnnoyingSkeleton(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initGoals")
    protected void newGoals(CallbackInfo ci) {
        if (!GlobalDifficultyManager.areSkeletonsBetter()) return;
        // flee Player
        goalSelector.add(3, new FleeEntityGoal<>(
                (AbstractSkeletonEntity) (Object) this,
                PlayerEntity.class,
                1.5f,
                1.2,
                1.3)
        );
    }

    @Inject(at = @At("RETURN"), method = "initGoals")
    protected void betterGoals(CallbackInfo ci) {
        if (GlobalDifficultyManager.areSkeletonsBetter()) MobUtils.commonBetterGoals(this, targetSelector);
    }
}
