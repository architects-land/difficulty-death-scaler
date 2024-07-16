package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        final var difficultyManager = new DifficultyManager();

        final LiteralArgumentBuilder<ServerCommandSource> command = literal("difficultydeathscaler");
        command.then(literal("get").executes(context -> {
            final var source = context.getSource();
            final var server = source.getServer();
            source.sendFeedback(() -> Text.literal(difficultyManager.getDifficultyUpdate(server, server.getOverworld().getDifficulty())), false);
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("set")
                .requires(source -> source.hasPermissionLevel(1))
                .then(argument("number of death", IntegerArgumentType.integer())
                .executes(context -> {
                    final var source = context.getSource();
                    final var server = source.getServer();
                    difficultyManager.setNumberOfDeath(server, IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> Text.literal("The difficulty has been changed"), true);
                    return Command.SINGLE_SUCCESS;
                })
                )
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));


        // set up difficulty of deathSteps[0]
        ServerLifecycleEvents.SERVER_STARTED.register(server -> difficultyManager.setNumberOfDeath(server, difficultyManager.getNumberOfDeath(), true));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                difficultyManager.increaseDeath(player.server);
                return;
            }
            BossManager.handleKill(entity, difficultyManager);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> difficultyManager.applyHealthModifierToPlayer(handler.player));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> difficultyManager.applyHealthModifierToPlayer(newPlayer));

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> BossManager.handleBuff(player, world, hand, entity));
    }
}