package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class BountyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("bounty").executes(context -> {
			context.getSource().sendSuccess(Bounty::getBountiesMessage, false);
			return Command.SINGLE_SUCCESS;
		}));
	}
}
