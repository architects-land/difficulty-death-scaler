package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private GlobalDifficultyManager difficultyManager = null;

    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_TEMP_BAN = GameRuleRegistry.register(
            MOD_ID +":enableTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true)
    );
    public static final GameRules.Key<GameRules.IntRule> DEATH_BEFORE_TEMP_BAN = GameRuleRegistry.register(
            MOD_ID +":deathBeforeTempBan",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(5)
    );
    public static final GameRules.Key<GameRules.IntRule> TEMP_BAN_DURATION = GameRuleRegistry.register(
            MOD_ID +":tempBanDuration",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(12)
    );

    private final Map<UUID, PlayerDifficultyManager> playerDifficultyManagerMap = new HashMap<>();
    private final Map<UUID, Bounty> bountyMap = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        // New command: /dds [get|set]
        // /dds get [none|player]
        // /dds set [global|player]
        // /dds set global [int]
        // /dds set player [player] [difficulty|daily-death] [int]
        // /dds help

        final Command<ServerCommandSource> globalGetExecute = context -> {
            final var source = context.getSource();
            final var server = source.getServer();
            source.sendFeedback(() -> {
                return Text.literal(difficultyManager.getDifficultyUpdate(server.getOverworld().getDifficulty()));
            }, false);
            return Command.SINGLE_SUCCESS;
        };

        final var getCommand = literal("get").then(
                argument("player", EntityArgumentType.player()).executes(context -> {
                    final var source = context.getSource();
                    final var server = source.getServer();
                    final var target = EntityArgumentType.getPlayer(context, "player");
                    source.sendFeedback(() -> Text.literal(
                            getPlayerDifficultyManager(server, target)
                                    .getDifficultyUpdate(server.getOverworld().getDifficulty())
                    ), false);
                    return Command.SINGLE_SUCCESS;
                })
        ).executes(globalGetExecute);

        final var setGlobalCommand = literal("global").then(
                argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    final var source = context.getSource();
                    difficultyManager.setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> Text.literal("The difficulty has been changed"), true);
                    return Command.SINGLE_SUCCESS;
                })
        );

        final var setPlayerCommand = literal("player").then(argument("player", EntityArgumentType.player()).then(
                literal("difficulty").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    final var source = context.getSource();
                    final var server = source.getServer();
                    final var target = EntityArgumentType.getPlayer(context, "player");
                    getPlayerDifficultyManager(server, target).setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> {
                        return Text.literal("The difficulty has been changed for ").append(target.getDisplayName());
                    }, true);
                    target.sendMessage(Text.literal("Your difficulty has been changed by ").append(source.getDisplayName()));
                    return Command.SINGLE_SUCCESS;
                }))
        ).then(
                literal("daily-death").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Not implemented yet"), false);
                    return Command.SINGLE_SUCCESS;
                }))
        ));

        final var setCommand = literal("set")
                .requires(source -> source.hasPermissionLevel(2))
                .then(setGlobalCommand)
                .then(setPlayerCommand);

        final var helpCommand = literal("help").executes(context -> {
            final var url = "https://architects-land.github.io/difficulty-death-scaler/";
            final var link = Text.literal(url);
            link.fillStyle(
                link.getStyle()
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                    .withFormatting(Formatting.UNDERLINE)
            );
            context.getSource().sendFeedback(() ->
                    Text.literal("The wiki is available at ").append(link),
                    false);
            return Command.SINGLE_SUCCESS;
        });

        final LiteralArgumentBuilder<ServerCommandSource> command = literal("difficultydeathscaler");
        command.then(setCommand);
        command.then(getCommand);
        command.then(helpCommand);

        final LiteralArgumentBuilder<ServerCommandSource> commandShort = literal("dds");
        commandShort.then(setCommand);
        commandShort.then(getCommand);
        commandShort.then(helpCommand);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(command);
            dispatcher.register(commandShort);
            dispatcher.register(literal("ddsg").executes(globalGetExecute));
            dispatcher.register(literal("ddsp").executes(context -> {
                final var source = context.getSource();
                final var server = source.getServer();
                final var target = source.getPlayer();
                if (target == null) {
                    source.sendFeedback(() -> Text.literal("You are not a player"), false);
                    return 2;
                }
                source.sendFeedback(() -> Text.literal(
                        getPlayerDifficultyManager(server, target)
                                .getDifficultyUpdate(server.getOverworld().getDifficulty())
                ), false);
                return Command.SINGLE_SUCCESS;
            }));
        });

        // set up difficulty of deathSteps[0]
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            difficultyManager = new GlobalDifficultyManager(server);
            loadAllPlayerManagers(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            difficultyManager.save();
            playerDifficultyManagerMap.forEach((player, manager) -> {
                manager.save();
            });
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

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> BossManager.handleBuff(player, world, hand, entity));

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof HostileEntity)) return;
            difficultyManager.onEntitySpawn((HostileEntity) entity);
        });
    }

    private PlayerDifficultyManager getPlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        if (playerDifficultyManagerMap.containsKey(player.getUuid())) {
            final var playerDifficulty = playerDifficultyManagerMap.get(player.getUuid());
            playerDifficulty.player = player;
            return playerDifficulty;
        }
        final var playerDifficulty = new PlayerDifficultyManager(server, difficultyManager, player);
        playerDifficultyManagerMap.put(player.getUuid(), playerDifficulty);
        return playerDifficulty;
    }

    private void loadAllPlayerManagers(MinecraftServer server) {
        final var state = StateSaver.getServerState(server);
        state.players.forEach((uuid, data) -> {
            playerDifficultyManagerMap.computeIfAbsent(uuid, u -> new PlayerDifficultyManager(server, difficultyManager, uuid, data));
        });
    }
}