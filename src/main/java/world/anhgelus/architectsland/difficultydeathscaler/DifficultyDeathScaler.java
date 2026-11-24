package world.anhgelus.architectsland.difficultydeathscaler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.util.EventResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyCommand;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyState;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.BountyCommand;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.passive.PassiveDifficulty;
import world.anhgelus.architectsland.difficultydeathscaler.sleep.Sleeper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DifficultyDeathScaler implements ModInitializer {
	public static final String MOD_ID = "dds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final GameRule<Boolean> ENABLE_TEMP_BAN = GameRuleBuilder
		.forBoolean(true)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("enable_temp_ban"));
	public static final GameRule<Boolean> ENABLE_BOUNTY = GameRuleBuilder
		.forBoolean(true)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("enable_bounty"));
	public static final GameRule<Boolean> ENABLE_PASSIVE_DIFFICULTY = GameRuleBuilder
		.forBoolean(true)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("enable_passive_difficulty"));
	public static final GameRule<Boolean> ENABLE_REDACTED = GameRuleBuilder
		.forBoolean(true)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("enable_redacted"));
	public static final GameRule<Integer> DEATH_BEFORE_TEMP_BAN = GameRuleBuilder
		.forInteger(5)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("death_before_temps_ban"));
	public static final GameRule<Integer> TEMP_BAN_DURATION = GameRuleBuilder
		.forInteger(12)
		.category(GameRuleCategory.MISC)
		.buildAndRegister(id("temp_ban_duration"));

	private final static Map<UUID, PlayerDifficultyManager> PLAYER_DIFFICULTY_MANAGERS =
		new HashMap<>();
	private static GlobalDifficultyManager GLOBAL_DIFFICULTY_MANAGER;

	public static Identifier id(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
	}

	/**
	 * Set player in difficulty manager
	 */
	public static PlayerDifficultyManager getPlayerDifficulty(ServerPlayer player) {
		return PLAYER_DIFFICULTY_MANAGERS.computeIfAbsent(player.getUUID(), _ ->
			new PlayerDifficultyManager(player.level().getServer(),
				GLOBAL_DIFFICULTY_MANAGER,
				DifficultyState.get(player)));
	}

	public static GlobalDifficultyManager getGlobalDifficulty() {
		return GLOBAL_DIFFICULTY_MANAGER;
	}

	private static void loadAllPlayerManagers(MinecraftServer server) {
		DifficultyState.get(server).getPlayers().forEach(it ->
			PLAYER_DIFFICULTY_MANAGERS.put(UUID.fromString(it.getKey()),
				new PlayerDifficultyManager(server, GLOBAL_DIFFICULTY_MANAGER, it.getValue())));
	}

	@Override
	public void onInitialize() {
		ModCriteria.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
			DifficultyCommand.register(dispatcher);
			BountyCommand.register(dispatcher);
		});

		// set up base difficulty and player difficulty fetcher
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			GLOBAL_DIFFICULTY_MANAGER = new GlobalDifficultyManager(server);
			loadAllPlayerManagers(server);

			//enforce gamerules to prevent a crash during sleeper event
			server.getGameRules().set(GameRules.ADVANCE_TIME, true, server);
		});

		ServerLifecycleEvents.BEFORE_SAVE.register((_, _, _) ->
			GLOBAL_DIFFICULTY_MANAGER.save(PLAYER_DIFFICULTY_MANAGERS));

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof ServerPlayer player)) {
				BossManager.handleKill(entity, damageSource, GLOBAL_DIFFICULTY_MANAGER);
				return;
			}
			PlayerListener.afterDeath(player, damageSource);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register(PlayerListener::afterRespawn);

		ServerPlayerEvents.JOIN.register(PlayerListener::onConnection);

		ServerPlayerEvents.LEAVE.register(PlayerListener::onDisconnection);

		UseEntityCallback.EVENT.register(PlayerListener::useItemCallback);

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof EnderDragon)
				BossManager.dragonLoaded((EnderDragon) entity, world);
			if (!(entity instanceof final Monster hostile)) return;
			GLOBAL_DIFFICULTY_MANAGER.onEntitySpawn(hostile);
			PassiveDifficulty.onEntitySpawn(hostile);
		});

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(
			(player, _, destination) -> {
				if (destination.dimension().identifier() != Level.END.identifier()) return;
				BossManager.playerEntersEnd(player, destination);
			});

		EntitySleepEvents.START_SLEEPING.register((entity, _) -> {
			if (!Sleeper.canSleep()) return;
			final var server = entity.level().getServer();
			if (server == null) {
				LOGGER.warn("Server is null");
				return;
			}
			if (!server.getGameRules().get(ENABLE_REDACTED)) return;
			if (!(entity instanceof Player)) return;
			if (entity.getRandom().nextFloat() * 100 >
				Sleeper.percentageToEmit(GLOBAL_DIFFICULTY_MANAGER.getNumberOfDeath()))
				return;
			if (Sleeper.tryEmitNewEvent(server)) LOGGER.info("Starting a new sleep event");
		});

		EntitySleepEvents.ALLOW_BED.register((entity, _, _, vanillaResult) -> {
			if (!(entity instanceof Player)) return EventResult.PASS;
			return vanillaResult && Sleeper.canSleep()
				? EventResult.PASS
				: EventResult.DENY;
		});

		LOGGER.info("Difficulty Death Scaler initialized");
	}
}