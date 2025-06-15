package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;

@Mixin(MobEntity.class)
public abstract class AnnoyingMobEntity extends LivingEntity {
    protected AnnoyingMobEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "getSafeFallDistance", cancellable = true)
    public void newSafeFallDistance(CallbackInfoReturnable<Integer> cir) {
        final var t = (MobEntity) (Object) this;
        if (t instanceof SkeletonEntity && GlobalDifficultyManager.areSkeletonsBetter()) {
            cir.setReturnValue(safeFallDistance(t.getTarget()));
        } else if (t instanceof ZombieEntity && GlobalDifficultyManager.areZombiesBetter()) {
            cir.setReturnValue(safeFallDistance(t.getTarget()));
        }
    }

    @Unique
    private int safeFallDistance(LivingEntity target) {
        return target == null ? getSafeFallDistance(0.0F) : getSafeFallDistance(this.getHealth() - 3.0F);
    }
}
