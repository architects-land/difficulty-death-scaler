package world.anhgelus.architectsland.difficultydeathscaler.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import java.util.Random;

public class Getters {
    public static final Random RANDOM = new Random();

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

    public static Getters.ProfileDifficultyGetter PROFILE_DIFFICULTY_GETTER;

    public static Getters.PlayerDifficultyGetter PLAYER_DIFFICULTY_GETTER;

    public static Getters.GlobalDifficultyGetter GLOBAL_DIFFICULTY_GETTER;
}
