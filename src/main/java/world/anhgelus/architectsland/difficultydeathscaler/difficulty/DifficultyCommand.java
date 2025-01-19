package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DifficultyCommand {

    /**
     * Functional interface giving the player difficulty manager
     */
    @FunctionalInterface
    public interface PlayerDifficultyGetter {
        PlayerDifficultyManager get(MinecraftServer server, ServerPlayerEntity player);
    }

    /**
     * Functional interface giving the global difficulty manager
     */
    @FunctionalInterface
    public interface GlobalDifficultyGetter {
        GlobalDifficultyManager get();
    }

    private static PlayerDifficultyGetter playerDifficultyGetter;

    public static void setPlayerDifficultyGetter(PlayerDifficultyGetter playerDifficultyGetter) {
        DifficultyCommand.playerDifficultyGetter = playerDifficultyGetter;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, GlobalDifficultyGetter globalDifficultyGetter) {
        final Command<ServerCommandSource> globalGetExecute = context -> {
            final var source = context.getSource();
            final var server = source.getServer();
            source.sendFeedback(() -> {
                return Text.literal(globalDifficultyGetter.get().getDifficultyUpdate(server.getOverworld().getDifficulty()));
            }, false);
            return Command.SINGLE_SUCCESS;
        };

        // is /dds get [none|player]
        final var getCommand = literal("get").then(
                argument("player", EntityArgumentType.player()).executes(context -> {
                    return sendPlayerDifficulty(context, EntityArgumentType.getPlayer(context, "player"));
                })
        ).executes(globalGetExecute);

        // is /dds set global [int]
        final var setGlobalCommand = literal("global").then(
                argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    final var source = context.getSource();
                    globalDifficultyGetter.get().setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> Text.literal("The difficulty has been changed"), true);
                    return Command.SINGLE_SUCCESS;
                })
        );

        // is /dds set player [player] [difficulty|daily-death] [int]
        final var setPlayerCommand = literal("player").then(argument("player", EntityArgumentType.player())
            .then(
                literal("difficulty").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    final var source = context.getSource();
                    final var server = source.getServer();
                    final var target = EntityArgumentType.getPlayer(context, "player");
                    playerDifficultyGetter.get(server, target).setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"), false);
                    source.sendFeedback(() -> {
                        return Text.literal("The difficulty has been changed for ").append(target.getDisplayName());
                    }, true);
                    target.sendMessage(Text.literal("Your difficulty has been changed by ").append(source.getDisplayName()));
                    return Command.SINGLE_SUCCESS;
                }))
            ).then(
                literal("daily-death").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Not implemented yet"), false);
                    final var source = context.getSource();
                    final var server = source.getServer();
                    final var target = EntityArgumentType.getPlayer(context, "player");
                    playerDifficultyGetter.get(server, target).setDeathDay(IntegerArgumentType.getInteger(context, "number of death"));
                    source.sendFeedback(() -> {
                        return Text.literal("The difficulty has been changed for ").append(target.getDisplayName());
                    }, true);
                    target.sendMessage(Text.literal("Your difficulty has been changed by ").append(source.getDisplayName()));
                    return Command.SINGLE_SUCCESS;
                }))
        ));

        // is /dds set [global|player]
        final var setCommand = literal("set")
                .requires(source -> source.hasPermissionLevel(2))
                .then(setGlobalCommand)
                .then(setPlayerCommand);

        // is /dds help
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

        // is long /dds
        final LiteralArgumentBuilder<ServerCommandSource> command = literal("difficultydeathscaler");
        command.then(setCommand);
        command.then(getCommand);
        command.then(helpCommand);

        // is /dds
        final LiteralArgumentBuilder<ServerCommandSource> commandShort = literal("dds");
        commandShort.then(setCommand);
        commandShort.then(getCommand);
        commandShort.then(helpCommand);

        dispatcher.register(command);
        dispatcher.register(commandShort);
        // is shortcut /dds
        dispatcher.register(literal("ddsg").executes(globalGetExecute));
        dispatcher.register(literal("ddsp").executes(context -> {
            final var source = context.getSource();
            final var target = source.getPlayer();
            if (target == null) {
                source.sendFeedback(() -> Text.literal("You are not a player"), false);
                return 2;
            }
            return sendPlayerDifficulty(context, target);
        }));
    }

    private static int sendPlayerDifficulty(CommandContext<ServerCommandSource> context, ServerPlayerEntity target) {
        final var source = context.getSource();
        final var server = source.getServer();
        source.sendFeedback(() -> Text.literal(
                playerDifficultyGetter.get(server, target)
                        .getDifficultyUpdate(server.getOverworld().getDifficulty())
        ), false);
        return Command.SINGLE_SUCCESS;
    }
}
