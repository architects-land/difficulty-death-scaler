package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.net.URI;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static world.anhgelus.architectsland.difficultydeathscaler.utils.Getters.GLOBAL_DIFFICULTY_GETTER;
import static world.anhgelus.architectsland.difficultydeathscaler.utils.Getters.PLAYER_DIFFICULTY_GETTER;

public class DifficultyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		final Command<CommandSourceStack> globalGetExecute = context -> {
			final var source = context.getSource();
			final var server = source.getServer();
			source.sendSuccess(() -> {
				return GLOBAL_DIFFICULTY_GETTER.get().getDifficultyUpdate(server.overworld().getDifficulty());
			}, false);
			return Command.SINGLE_SUCCESS;
		};

		// is /dds get [none|player]
		final var getCommand = literal("get").then(
			argument("player", EntityArgument.player()).executes(context -> {
				return sendPlayerDifficulty(context, EntityArgument.getPlayer(context, "player"));
			})
		).executes(globalGetExecute);

		// is /dds set global [int]
		final var setGlobalCommand = literal("global").then(
			argument("number of death", IntegerArgumentType.integer()).executes(context -> {
				final var source = context.getSource();
				GLOBAL_DIFFICULTY_GETTER.get().setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"));
				source.sendSuccess(() -> Component.literal("The difficulty has been changed"), true);
				return Command.SINGLE_SUCCESS;
			})
		);

		// is /dds set player [player] [difficulty|daily-death] [int]
		final var setPlayerCommand = literal("player").then(argument("player", EntityArgument.player())
			.then(
				literal("difficulty").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
					final var source = context.getSource();
					final var server = source.getServer();
					final var target = EntityArgument.getPlayer(context, "player");
					PLAYER_DIFFICULTY_GETTER.get(server, target).setNumberOfDeath(IntegerArgumentType.getInteger(context, "number of death"));
					source.sendSuccess(() -> {
						return Component.literal("The difficulty has been changed for ").append(target.getDisplayName());
					}, true);
					target.sendSystemMessage(Component.literal("Your difficulty has been changed by ").append(source.getDisplayName()));
					return Command.SINGLE_SUCCESS;
				}))
			).then(
				literal("daily-death").then(argument("number of death", IntegerArgumentType.integer()).executes(context -> {
					final var source = context.getSource();
					final var server = source.getServer();
					final var target = EntityArgument.getPlayer(context, "player");
					PLAYER_DIFFICULTY_GETTER.get(server, target).setDeathDay(IntegerArgumentType.getInteger(context, "number of death"));
					source.sendSuccess(() -> {
						return Component.literal("The difficulty has been changed for ").append(target.getDisplayName());
					}, true);
					target.sendSystemMessage(Component.literal("Your difficulty has been changed by ").append(source.getDisplayName()));
					return Command.SINGLE_SUCCESS;
				}))
			));

		// is /dds set [global|player]
		final var setCommand = literal("set")
			.requires(source ->
				source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
			.then(setGlobalCommand)
			.then(setPlayerCommand);

		// is /dds help
		final var helpCommand = literal("help").executes(context -> {
			final var url = "https://architects-land.github.io/difficulty-death-scaler/";
			final var link = Component.literal(url);
			link.withStyle(
				link.getStyle()
					.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
					.applyFormat(ChatFormatting.UNDERLINE)
			);
			context.getSource().sendSuccess(() ->
					Component.literal("The wiki is available at ").append(link),
				false);
			return Command.SINGLE_SUCCESS;
		});

		// is long /dds
		final LiteralArgumentBuilder<CommandSourceStack> command = literal("difficultydeathscaler");
		command.then(setCommand);
		command.then(getCommand);
		command.then(helpCommand);

		// is /dds
		final LiteralArgumentBuilder<CommandSourceStack> commandShort = literal("dds");
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
				source.sendSuccess(() -> Component.literal("You are not a player"), false);
				return 2;
			}
			return sendPlayerDifficulty(context, target);
		}));
	}

	private static int sendPlayerDifficulty(CommandContext<CommandSourceStack> context, ServerPlayer target) {
		final var source = context.getSource();
		final var server = source.getServer();
		source.sendSuccess(() ->
			PLAYER_DIFFICULTY_GETTER.get(server, target)
				.getDifficultyUpdate(server.overworld().getDifficulty()
				), false);
		return Command.SINGLE_SUCCESS;
	}
}
