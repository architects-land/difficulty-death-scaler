package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class BountyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bounty").executes(context -> {
            context.getSource().sendFeedback(Bounty::getBountiesMessage, false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
