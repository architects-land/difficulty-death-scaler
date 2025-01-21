package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ChaseBoatGoal;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(DrownedEntity.class)
public abstract class AnnoyingDrowned extends HostileEntity {
    protected AnnoyingDrowned(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initCustomGoals")
    protected void newGoals(CallbackInfo ci) {
        if (!GlobalDifficultyManager.areZombiesBetter()) return;
        goalSelector.add(3, new ChaseBoatGoal(this));
        MobUtils.commonBetterGoals(this, targetSelector);
    }
}
