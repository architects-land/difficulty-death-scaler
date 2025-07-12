package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyCommand;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.BountyCommand;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.listener.PlayerListener;
import world.anhgelus.architectsland.difficultydeathscaler.passive.PassiveDifficulty;
import world.anhgelus.architectsland.difficultydeathscaler.sleep.Sleeper;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final String GAMERULE_PREFIX = "dds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_TEMP_BAN = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":enableTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true, (server, rule) -> {
                PlayerDifficultyManager.ENABLE_TEMP_BAN = rule.get();
            })
    );
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_BOUNTY = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":enableBounty",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true, (server, rule) -> {
                Bounty.ENABLED = rule.get();
            })
    );
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_PASSIVE_DIFFICULTY = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":enablePassiveDifficulty",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true, (server, rule) -> {
                PassiveDifficulty.ENABLED = rule.get();
            })
    );
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_REDACTED = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":enableRedacted",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true, (server, rule) -> {
                Sleeper.ENABLED = rule.get();
            })
    );
    public static final GameRules.Key<GameRules.IntRule> DEATH_BEFORE_TEMP_BAN = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":deathBeforeTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(5, (server, rule) -> {
                PlayerDifficultyManager.DEATH_BEFORE_TEMP_BAN = rule.get();
            })
    );
    public static final GameRules.Key<GameRules.IntRule> TEMP_BAN_DURATION = GameRuleRegistry.register(
            GAMERULE_PREFIX + ":tempBanDuration",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(12, (server, rule) -> {
                PlayerDifficultyManager.TEMP_BAN_DURATION = rule.get();
            })
    );
    private final Map<UUID, PlayerDifficultyManager> playerDifficultyManagerMap = new HashMap<>();
    private GlobalDifficultyManager difficultyManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler initialized");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DifficultyCommand.register(dispatcher);
            BountyCommand.register(dispatcher);
        });

        // set up base difficulty and player difficulty fetcher
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            difficultyManager = new GlobalDifficultyManager(server);
            loadAllPlayerManagers(server);

            //enforce gamerules to prevent a crash during sleeper event
            server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);

            Getters.PLAYER_DIFFICULTY_GETTER = this::getPlayerDifficultyManager;
            Getters.GLOBAL_DIFFICULTY_GETTER = () -> difficultyManager;

            TimerAccess.getTimerFromOverworld(server).dds_runTask(new TickTask(() -> {
                LOGGER.info("Difficulty Death Scaler is saving...");
                difficultyManager.save();
                playerDifficultyManagerMap.forEach((player, manager) -> manager.save());
                LOGGER.info("Difficulty Death Scaler saved");
            }, 20 * 60 * 20, 20 * 60 * 20));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            difficultyManager.save();
            playerDifficultyManagerMap.forEach((player, manager) -> manager.save());
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                BossManager.handleKill(entity, difficultyManager);
                return;
            }
            PlayerListener.afterDeath(player, damageSource);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register(PlayerListener::afterRespawn);

        ServerPlayConnectionEvents.JOIN.register(PlayerListener::onConnection);

        ServerPlayConnectionEvents.DISCONNECT.register(PlayerListener::onDisconnection);

        UseEntityCallback.EVENT.register(PlayerListener::useItemCallback);

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof EnderDragonEntity) BossManager.dragonLoaded((EnderDragonEntity) entity, world);
            if (!(entity instanceof final HostileEntity hostile)) return;
            difficultyManager.onEntitySpawn(hostile);
            PassiveDifficulty.onEntitySpawn(hostile);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (destination.getRegistryKey().getValue() != World.END.getValue()) return;
            BossManager.playerEntersEnd(player, destination);
        });

        EntitySleepEvents.START_SLEEPING.register((entity, pos) -> {
            if (!Sleeper.canSleep()) return;
            if (!Sleeper.ENABLED) return;
            if (!(entity instanceof PlayerEntity)) return;
            // if the number is too high, return
            if (entity.getRandom().nextFloat() * 100 > Sleeper.percentageToEmit(difficultyManager.getNumberOfDeath()))
                return;
            // try starting a new event
            final var server = entity.getServer();
            if (server == null) {
                LOGGER.warn("Server is null");
                return;
            }
            if (Sleeper.tryEmitNewEvent(server)) LOGGER.info("Starting a new sleep event");
        });

        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
            if (!(entity instanceof PlayerEntity)) return ActionResult.PASS;
            return Sleeper.canSleep() ? ActionResult.PASS : ActionResult.FAIL;
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
        return playerDifficultyManagerMap.computeIfAbsent(profile.getId(), id -> {
            return new PlayerDifficultyManager(
                    server, difficultyManager, id, StateSaver.getPlayerState(server, profile.getId())
            );
        });
    }

    private void loadAllPlayerManagers(MinecraftServer server) {
        final var state = StateSaver.getServerState(server);
        state.players.forEach((uuid, data) -> {
            playerDifficultyManagerMap.computeIfAbsent(uuid, u -> new PlayerDifficultyManager(server, difficultyManager, uuid, data));
        });
    }
}