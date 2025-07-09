package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

public class Bounty extends DifficultyTimer {
    public static final double BOUNTY_DEATH_PERCENTAGE = 0.03;
    public static final int BOUNTY_ENABLED_AFTER = 20;

    public static boolean ENABLED = true;

    private final GlobalDifficultyManager globalDifficulty;
    private final PlayerDifficultyManager playerDifficulty;
    private ServerPlayerEntity player;

    private boolean enabled = false;

    private Bounty(MinecraftServer server, GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
        if (!ENABLED) throw new IllegalStateException("Bounty is disabled");
        timer = TimerAccess.getTimerFromOverworld(server);
        this.globalDifficulty = globalDifficulty;
        this.playerDifficulty = playerDifficulty;
        this.player = playerDifficulty.player;

        final var delay = server.getOverworld().getRandom().nextInt(2) + 1;

        DifficultyDeathScaler.LOGGER.info("New bounty {} broadcast in {} minutes", this, delay * 5);

        timer.dds_runTask(new TickTask(() -> {
            enabled = true;
            bountyBroadcast();
        }, Math.round(delay * 5 * 60 * 20L)));
    }

    @Nullable
    public static Bounty newBounty(MinecraftServer server, GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
        if (!ENABLED) return null;
        if (globalDifficulty.getTotalOfDeath() >= BOUNTY_ENABLED_AFTER &&
                (double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() <= BOUNTY_DEATH_PERCENTAGE
        ) return new Bounty(server, globalDifficulty, playerDifficulty);
        return null;
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
        enabled = false;
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

        if (player.getServer() == null) {
            DifficultyDeathScaler.LOGGER.warn("Server is null");
            return;
        }
        player.getServer().getPlayerManager().broadcast(Text.of(sb.toString()), false);

        final var n = attackerDifficulty.getNumberOfDeath();
        attackerDifficulty.setNumberOfDeath(playerDifficulty.getNumberOfDeath(), false);
        playerDifficulty.setNumberOfDeath(n, false);
    }

    public void onDeath() {
        if (!enabled) return;
        player = playerDifficulty.player;
        if ((double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() > BOUNTY_DEATH_PERCENTAGE)
            enabled = false;
    }

    public void onDisconnect() {
        if (!enabled) return;
        enabled = false;
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

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String toString() {
        return String.format(
                "Bounty(bountyDeathPercentage=%f, bountyEnabledAfter=%d, player=%s, player_totalDeath=%s, global_totalDeath=%s, player_deathPercentage=%f)",
                BOUNTY_DEATH_PERCENTAGE, BOUNTY_ENABLED_AFTER, player.getName().getString(), playerDifficulty.getTotalOfDeath(), globalDifficulty.getTotalOfDeath(),
                playerDifficulty.getTotalOfDeath() / (double) globalDifficulty.getTotalOfDeath()
        );
    }
}
