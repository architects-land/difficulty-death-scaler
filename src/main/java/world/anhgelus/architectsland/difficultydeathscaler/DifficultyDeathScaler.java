package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private GlobalDifficultyManager difficultyManager = null;

    private final Map<ServerPlayerEntity, PlayerDifficultyManager> playerDifficultyManagerMap = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        final LiteralArgumentBuilder<ServerCommandSource> command = literal("difficultydeathscaler");
        command.then(literal("get").executes(context -> {
            final var source = context.getSource();
            final var server = source.getServer();
            source.sendFeedback(() -> Text.literal(difficultyManager.getDifficultyUpdate(server.getOverworld().getDifficulty())), false);
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("set")
                .requires(source -> source.hasPermissionLevel(1))
                .then(argument("number of death", IntegerArgumentType.integer())
                .executes(context -> {
                    final var source = context.getSource();
                    difficultyManager.setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> Text.literal("The difficulty has been changed"), true);
                    return Command.SINGLE_SUCCESS;
                })
                )
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));

        // set up difficulty of deathSteps[0]
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            difficultyManager = new GlobalDifficultyManager(server);
            difficultyManager.setNumberOfDeath(difficultyManager.getNumberOfDeath(), true);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            difficultyManager.save();
            playerDifficultyManagerMap.forEach((player, manager) -> {
                manager.save();
            });
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity) {
                difficultyManager.increaseDeath();
                return;
            }
            BossManager.handleKill(entity, difficultyManager);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            difficultyManager.applyModifiers(handler.player);
            getPlayerDifficultyManager(server, handler.player).applyModifiers();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            difficultyManager.applyModifiers(newPlayer);
            final var playerDifficulty = getPlayerDifficultyManager(newPlayer.server, newPlayer);
            playerDifficulty.player = newPlayer;
            playerDifficulty.increaseDeath();
            playerDifficulty.applyModifiers();
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> BossManager.handleBuff(player, world, hand, entity));
    }

    private PlayerDifficultyManager getPlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        return playerDifficultyManagerMap.computeIfAbsent(player, p -> new PlayerDifficultyManager(server, p));
    }
}