package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;

import java.util.Timer;
import java.util.TimerTask;

public class Bounty extends DifficultyTimer {
    public static final double BOUNTY_DEATH_PERCENTAGE = 0.02;
    public static final int BOUNTY_ENABLED_AFTER = 30;

    private final GlobalDifficultyManager globalDifficulty;
    private final PlayerDifficultyManager playerDifficulty;
    private ServerPlayerEntity player;

    private boolean enabled = false;

    private Bounty(GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
        timer = new Timer();
        this.globalDifficulty = globalDifficulty;
        this.playerDifficulty = playerDifficulty;
        this.player = playerDifficulty.player;

        final var delay = 1 + Math.random() * (2 - 1);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                enabled = true;
                bountyBroadcast();
            }
        }, Math.round(delay * 5 * 60 * 1000L));
    }

    private void bountyBroadcast() {
        final var sb = new StringBuilder();
        sb.append("§8==================== §rBounty! §8====================§r\n");
        sb.append("A bounty is put on ");

        var name = "";
        if (player.getDisplayName() == null) name = player.getName().getString();
        else name = player.getDisplayName().getString();

        sb.append(name);
        sb.append(" because he ");
        if (playerDifficulty.getTotalOfDeath() == 0) {
            sb.append("never died!");
        } else if (playerDifficulty.getTotalOfDeath() == 1) {
            sb.append("died only once!");
        } else {
            sb.append("died only ").append(playerDifficulty.getTotalOfDeath()).append(" times!");
        }

        sb.append("\n\n");
        sb.append("If you kill ").append(name).append(", you will swap your personal difficulty!\n\n");
        sb.append("Good luck!\n");
        sb.append("§8=============================================§r");

        if (player.getServer() == null) return;
        player.getServer().getPlayerManager().broadcast(Text.of(sb.toString()), false);
    }

    public void onKill(PlayerDifficultyManager attackerDifficulty) {
        if (!enabled) return;
        disable();
        assert attackerDifficulty.player != null;
        final var sb = new StringBuilder();
        sb.append("§8==================== §rBounty! §8====================§r\n");

        var name = "";
        if (player.getDisplayName() == null) name = player.getName().getString();
        else name = player.getDisplayName().getString();

        var attackerName = "";
        if (attackerDifficulty.player.getDisplayName() == null) attackerName = player.getName().getString();
        else attackerName = attackerDifficulty.player.getDisplayName().getString();

        sb.append(attackerName).append(" killed ").append(name).append("!\n");
        sb.append("They swap their player difficulty!\n");
        sb.append("§8=============================================§r");

        if (player.getServer() == null) return;
        player.getServer().getPlayerManager().broadcast(Text.of(sb.toString()), false);

        final var n = attackerDifficulty.getNumberOfDeath();
        attackerDifficulty.setNumberOfDeath(playerDifficulty.getNumberOfDeath(), false);
        playerDifficulty.setNumberOfDeath(n, false);
    }

    public void onDeath() {
        if (!enabled) return;
        player = playerDifficulty.player;
        if ((double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() > BOUNTY_DEATH_PERCENTAGE)
            disable();
    }

    public void onDisconnect() {
        if (!enabled) return;
        disable();
        final var sb = new StringBuilder();
        sb.append("§8==================== §rBounty! §8====================§r\n");

        var name = "";
        if (player.getDisplayName() == null) name = player.getName().getString();
        else name = player.getDisplayName().getString();

        sb.append(name).append(" disconnected. The bounty will be back when he/she is next connected!\n");
        sb.append("§8=============================================§r");

        if (player.getServer() == null) return;
        player.getServer().getPlayerManager().broadcast(Text.of(sb.toString()), false);
    }

    private void disable() {
        enabled = false;
        stop();
    }

    @Override
    public void stop() {
        if (!enabled) return;
        disable();
    }

    public static @Nullable Bounty newBounty(GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
        if (globalDifficulty.getTotalOfDeath() >= BOUNTY_ENABLED_AFTER &&
                (double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() <= BOUNTY_DEATH_PERCENTAGE
        ) {
            return new Bounty(globalDifficulty, playerDifficulty);
        }
        return null;
    }
}
