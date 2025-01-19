package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import java.net.SocketAddress;

import static world.anhgelus.architectsland.difficultydeathscaler.utils.Getters.PROFILE_DIFFICULTY_GETTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(at = @At("HEAD"), method = "checkCanJoin", cancellable = true)
    private void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        if (PROFILE_DIFFICULTY_GETTER == null) return;
        final var difficulty = PROFILE_DIFFICULTY_GETTER.get(profile);
        if (difficulty == null) return;
        if (difficulty.diedTooMuch())
            cir.setReturnValue(PlayerDifficultyManager.KICKED_DIED_TOO_MUCH_MESSAGE);
    }
}
