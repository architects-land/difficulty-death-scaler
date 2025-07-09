package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import static net.minecraft.server.command.CommandManager.literal;

public class BountyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bounty").executes(context -> {
            final var sb = new StringBuilder();
            sb.append("Bounties: \n");
            Getters.BOUNTIES_GETTER.get().forEach(bounty -> {
                if (!bounty.isEnabled()) return;
                var name = "";
                if (bounty.getPlayer().getDisplayName() == null) name = bounty.getPlayer().getName().getString();
                else name = bounty.getPlayer().getDisplayName().getString();
                sb.append("\n- ").append(name);
            });
            context.getSource().sendFeedback(() -> Text.of(sb.toString()), false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
