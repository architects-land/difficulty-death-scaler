package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyCommand;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private GlobalDifficultyManager difficultyManager;

    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_TEMP_BAN = GameRuleRegistry.register(
            MOD_ID + ":enableTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true)
    );
    public static final GameRules.Key<GameRules.IntRule> DEATH_BEFORE_TEMP_BAN = GameRuleRegistry.register(
            MOD_ID + ":deathBeforeTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(5)
    );
    public static final GameRules.Key<GameRules.IntRule> TEMP_BAN_DURATION = GameRuleRegistry.register(
            MOD_ID + ":tempBanDuration",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(12)
    );

    private final Map<UUID, PlayerDifficultyManager> playerDifficultyManagerMap = new HashMap<>();
    private final Map<UUID, Bounty> bountyMap = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Getters.PLAYER_DIFFICULTY_GETTER = this::getPlayerDifficultyManager;
            Getters.GLOBAL_DIFFICULTY_GETTER = () -> difficultyManager;
            DifficultyCommand.register(dispatcher);
        });

        // set up base difficulty and player difficulty fetcher
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            difficultyManager = new GlobalDifficultyManager(server);
            loadAllPlayerManagers(server);

            Getters.PROFILE_DIFFICULTY_GETTER = (profile) -> getPlayerDifficultyManager(server, profile);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            difficultyManager.save();
            playerDifficultyManagerMap.forEach((player, manager) -> manager.save());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity)) {
                BossManager.handleKill(entity, difficultyManager);
                return;
            }
            difficultyManager.increaseDeath();

            final var bounty = bountyMap.get(entity.getUuid());
            if (bounty == null || !(damageSource.getAttacker() instanceof final ServerPlayerEntity player)) return;
            bounty.onKill(getPlayerDifficultyManager(player.server, player));
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            difficultyManager.applyModifiers(newPlayer);

            final var playerDifficulty = getPlayerDifficultyManager(newPlayer.server, newPlayer);
            playerDifficulty.player = newPlayer;
            playerDifficulty.increaseDeath();
            playerDifficulty.applyModifiers();

            final var bounty = bountyMap.get(newPlayer.getUuid());
            if (bounty == null) return;
            bounty.onDeath();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final var playerDifficulty = getPlayerDifficultyManager(server, handler.player);
            if (playerDifficulty.kickIfDiedTooMuch()) return;
            playerDifficulty.applyModifiers();

            difficultyManager.applyModifiers(handler.player);

            final var bounty = Bounty.newBounty(difficultyManager, playerDifficulty);
            if (bounty != null) bountyMap.put(handler.player.getUuid(), bounty);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            final var bounty = bountyMap.get(handler.player.getUuid());
            if (bounty == null) return;
            bounty.onDisconnect();
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            return BossManager.handleBuff(player, world, hand, entity);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof HostileEntity)) return;
            difficultyManager.onEntitySpawn((HostileEntity) entity);
        });
    }

    /**
     * Set player in difficulty manager
     */
    private PlayerDifficultyManager getPlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        final var difficulty = getPlayerDifficultyManager(server, player.getGameProfile());
        difficulty.player = player;
        return difficulty;
    }

    /**
     * Does not set player in difficulty manager!
     */
    private PlayerDifficultyManager getPlayerDifficultyManager(MinecraftServer server, GameProfile profile) {
        if (playerDifficultyManagerMap.containsKey(profile.getId())) {
            return playerDifficultyManagerMap.get(profile.getId());
        }
        final var playerDifficulty = new PlayerDifficultyManager(
                server,
                difficultyManager,
                profile.getId(),
                StateSaver.getPlayerState(server, profile.getId())
        );
        playerDifficultyManagerMap.put(profile.getId(), playerDifficulty);
        return playerDifficulty;
    }

    private void loadAllPlayerManagers(MinecraftServer server) {
        final var state = StateSaver.getServerState(server);
        state.players.forEach((uuid, data) -> {
            playerDifficultyManagerMap.computeIfAbsent(uuid, u -> new PlayerDifficultyManager(server, difficultyManager, uuid, data));
        });
    }
}