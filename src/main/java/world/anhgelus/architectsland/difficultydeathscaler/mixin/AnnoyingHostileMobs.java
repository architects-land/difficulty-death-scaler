package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(Mob.class)
public class AnnoyingHostileMobs {
	@Inject(at = @At("HEAD"), method = "isSunBurnTick", cancellable = true)
	protected void isAffectedByDaylight(CallbackInfoReturnable<Boolean> cir) {
		final var mob = (Mob) (Object) this;
		if (mob instanceof Phantom && MobUtils.PHANTOMS_NIGHTMARE) {
			cir.setReturnValue(false);
			return;
		}
		if (!(mob instanceof Monster)) return;
		if (MobUtils.HOSTILE_MOBS_BURN) return;
		cir.setReturnValue(false);
	}
}
