package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bounty extends DifficultyTimer {
    public static final double BOUNTY_DEATH_PERCENTAGE = 0.03;
    public static final int BOUNTY_ENABLED_AFTER = 20;
    private static final Map<UUID, Bounty> bounties = new HashMap<>();
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
        assert playerDifficulty.player != null;
        if (bounties.containsKey(playerDifficulty.player.getUuid())) {
            DifficultyDeathScaler.LOGGER.warn("Bounty already exists for player {}", playerDifficulty.player.getUuid());
            return null;
        }
        if (globalDifficulty.getTotalOfDeath() >= BOUNTY_ENABLED_AFTER &&
                (double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() <= BOUNTY_DEATH_PERCENTAGE
        ) {
            final var b = new Bounty(server, globalDifficulty, playerDifficulty);
            bounties.put(playerDifficulty.player.getUuid(), b);
            return b;
        }
        return null;
    }

    @Nullable
    public static Bounty get(UUID uuid) {
        return bounties.get(uuid);
    }

    public static Collection<Bounty> get() {
        return bounties.values();
    }

    public static Text getBountiesMessage() {
        final var txt = Text.empty().append(Bounty.getBountyHeader());
        if (bounties.isEmpty()) {
            txt.append("Currently, there are no bounties.\n");
            txt.append(Bounty.getBountyFooter());
            return txt;
        }
        txt.append("Bounties:\n");
        get().forEach(bounty -> {
            if (!bounty.isEnabled()) return;
            final var name = Text.empty().formatted(Formatting.RED).append(bounty.getPlayer().getDisplayName());
            txt.append("- ").append(name).append("\n");
        });
        txt.append(Bounty.getBountyFooter());
        return txt;
    }

    private static Text getBountyHeader() {
        final var txt = Text.empty();
        txt.append(Text.literal("==================== ").formatted(Formatting.DARK_GRAY));
        txt.append("Bounty!");
        return txt.append(Text.literal(" ====================\n").formatted(Formatting.DARK_GRAY));
    }

    private static Text getBountyFooter() {
        return Text.literal("===============================================").formatted(Formatting.DARK_GRAY);
    }

    private void bountyBroadcast() {
        final var txt = Text.empty().append(getBountyHeader());
        txt.append("A bounty is put on ");
        txt.append(Text.empty().append(player.getDisplayName()).formatted(Formatting.RED));
        txt.append(" because they ");
        if (playerDifficulty.getTotalOfDeath() == 0) txt.append("never died!");
        else if (playerDifficulty.getTotalOfDeath() == 1) txt.append("died only once!");
        else txt.append(String.format("died only %d times!", playerDifficulty.getTotalOfDeath()));

        txt.append("\n\n");
        txt.append("If you kill ").append(player.getDisplayName()).append(", you will swap your personal difficulty!\n\n");
        txt.append("Good luck!\n");
        txt.append(getBountyFooter());

        if (player.getServer() == null) return;
        player.getServer().getPlayerManager().broadcast(txt, false);
    }

    public void onKill(PlayerDifficultyManager attackerDifficulty) {
        if (!enabled) return;
        enabled = false;
        assert attackerDifficulty.player != null;
        final var txt = Text.empty().append(getBountyHeader());
        txt.append(Text.empty().append(attackerDifficulty.player.getDisplayName()).formatted(Formatting.YELLOW));
        txt.append(" killed ");
        txt.append(Text.empty().append(player.getDisplayName()).formatted(Formatting.RED));
        txt.append("!\n");
        txt.append("They swap their player difficulty!\n");
        txt.append(getBountyFooter());

        if (player.getServer() == null) {
            DifficultyDeathScaler.LOGGER.warn("Server is null");
            return;
        }
        player.getServer().getPlayerManager().broadcast(txt, false);

        final var n = attackerDifficulty.getNumberOfDeath();
        attackerDifficulty.setNumberOfDeath(playerDifficulty.getNumberOfDeath());
        playerDifficulty.setNumberOfDeath(n);
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
        final var txt = Text.empty().append(getBountyHeader());
        txt.append(Text.empty().append(player.getDisplayName()).formatted(Formatting.RED));
        txt.append(" disconnected. The bounty will be back when they are next connected!\n");
        txt.append(getBountyFooter());

        if (player.getServer() == null) return;
        player.getServer().getPlayerManager().broadcast(txt, false);
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
