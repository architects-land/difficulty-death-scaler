package world.anhgelus.architectsland.difficultydeathscaler.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import java.util.UUID;

public class Getters {
    public static Getters.PlayerDifficultyGetter PLAYER_DIFFICULTY_GETTER;
    public static Getters.GlobalDifficultyGetter GLOBAL_DIFFICULTY_GETTER;
    public static Getters.BountyGetter BOUNTY_GETTER;

    /**
     * Functional interface giving the player difficulty manager with GameProfile
     */
    @FunctionalInterface
    public interface ProfileDifficultyGetter {
        PlayerDifficultyManager get(GameProfile profile);
    }

    /**
     * Functional interface giving the global difficulty manager
     */
    @FunctionalInterface
    public interface GlobalDifficultyGetter {
        GlobalDifficultyManager get();
    }

    /**
     * Functional interface giving the player difficulty manager
     */
    @FunctionalInterface
    public interface PlayerDifficultyGetter {
        PlayerDifficultyManager get(MinecraftServer server, ServerPlayerEntity player);
    }

    /**
     * Functional interface giving the bounty
     */
    @FunctionalInterface
    public interface BountyGetter {
        Bounty get(UUID uuid);
    }
}
