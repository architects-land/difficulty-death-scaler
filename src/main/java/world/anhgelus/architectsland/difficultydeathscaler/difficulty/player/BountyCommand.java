package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import static net.minecraft.server.command.CommandManager.literal;

public class BountyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bounty").executes(context -> {
            final var bounties = Getters.BOUNTIES_GETTER.get();
            final var txt = Text.empty().append(Bounty.getBountyHeader());
            if (bounties.isEmpty()) {
                txt.append("Currently, there are no bounties.\n");
                txt.append(Bounty.getBountyFooter());
                context.getSource().sendFeedback(() -> txt, false);
                return Command.SINGLE_SUCCESS;
            }
            txt.append("Bounties:\n");
            Getters.BOUNTIES_GETTER.get().forEach(bounty -> {
                if (!bounty.isEnabled()) return;
                final var name = Text.empty().formatted(Formatting.RED).append(bounty.getPlayer().getDisplayName());
                txt.append("- ").append(name).append("\n");
            });
            txt.append(Bounty.getBountyFooter());
            context.getSource().sendFeedback(() -> txt, false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
