package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(MobEntity.class)
public class AnnoyingHostileMobs {
    @Inject(at = @At("HEAD"), method = "isAffectedByDaylight", cancellable = true)
    protected void isAffectedByDaylight(CallbackInfoReturnable<Boolean> cir) {
        final var mob = (MobEntity) (Object) this;
        if (mob instanceof PhantomEntity && MobUtils.PHANTOMS_NIGHTMARE) {
            cir.setReturnValue(false);
            return;
        }
        if (!(mob instanceof HostileEntity)) return;
        if (MobUtils.HOSTILE_MOBS_BURN) return;
        cir.setReturnValue(false);
    }
}
