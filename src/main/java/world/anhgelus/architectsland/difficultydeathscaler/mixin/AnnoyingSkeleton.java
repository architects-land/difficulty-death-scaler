package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(AbstractSkeleton.class)
public abstract class AnnoyingSkeleton extends Monster {
	protected AnnoyingSkeleton(EntityType<? extends Monster> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method = "registerGoals")
	protected void newGoals(CallbackInfo ci) {
		if (!GlobalDifficultyManager.areSkeletonsBetter()) return;
		// flee Player
		goalSelector.addGoal(3, new AvoidEntityGoal<>(
			(AbstractSkeleton) (Object) this,
			Player.class,
			1.5f,
			1.2,
			1.3)
		);
	}

	@Inject(at = @At("RETURN"), method = "registerGoals")
	protected void betterGoals(CallbackInfo ci) {
		if (GlobalDifficultyManager.areSkeletonsBetter())
			MobUtils.commonBetterGoals(this, targetSelector);
	}
}
