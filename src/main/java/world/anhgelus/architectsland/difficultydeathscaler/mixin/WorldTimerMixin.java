package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.utils.TimerAccess;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class WorldTimerMixin implements TimerAccess {
    @Unique
    private long ticksUntilEnd;

    @Unique
    private End end;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (--ticksUntilEnd != 0) return;
        end.end();
    }

    @Override
    public void dds_setTimer(long ticksUntilEnd, End end) {
        this.ticksUntilEnd = ticksUntilEnd;
        this.end = end;
    }
}
