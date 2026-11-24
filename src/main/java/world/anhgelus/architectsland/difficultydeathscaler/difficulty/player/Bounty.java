package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.AdvancementProvider;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bounty extends DifficultyTimer {
	public static final double BOUNTY_DEATH_PERCENTAGE = 0.03;
	public static final int BOUNTY_ENABLED_AFTER = 20;
	private static final Map<UUID, Bounty> bounties = new HashMap<>();
	private final GlobalDifficultyManager globalDifficulty;
	private final PlayerDifficultyManager playerDifficulty;
	private final TimerAccess.TickTask task;
	private ServerPlayer player;
	private boolean enabled = false;

	private Bounty(MinecraftServer server, GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
		if (!server.getGameRules().get(DifficultyDeathScaler.ENABLE_BOUNTY))
			throw new IllegalStateException("Bounty is disabled");
		timer = TimerAccess.getTimerFromOverworld(server);
		this.globalDifficulty = globalDifficulty;
		this.playerDifficulty = playerDifficulty;
		this.player = playerDifficulty.player.orElseThrow();

		final var delay = server.overworld().getRandom().nextInt(2) + 1;

		DifficultyDeathScaler.LOGGER.info("New bounty {} broadcast in {} minutes", this, delay * 5);

		task = new TickTask(() -> {
			enabled = true;
			bountyBroadcast();
			server.getPlayerList().getPlayers().forEach(p -> {
				ModCriteria.BOUNTY.trigger(p, AdvancementProvider.BOUNTY_BE_PRESENT);
			});
			ModCriteria.BOUNTY.trigger(player, AdvancementProvider.BOUNTY_RECEIVE);
		}, Math.round(delay * 5 * 60 * 20L));

		timer.dds_runTask(task);
	}

	@Nullable
	public static Bounty newBounty(MinecraftServer server, GlobalDifficultyManager globalDifficulty, PlayerDifficultyManager playerDifficulty) {
		if (!server.getGameRules().get(DifficultyDeathScaler.ENABLE_BOUNTY)) return null;
		if (playerDifficulty.player.isEmpty()) return null;
		final var uuid = playerDifficulty.player.get().getUUID();
		if (bounties.containsKey(uuid)) {
			DifficultyDeathScaler.LOGGER.warn("Bounty already exists for player {}", uuid);
			return null;
		}
		if (globalDifficulty.getTotalOfDeath() >= BOUNTY_ENABLED_AFTER &&
			(double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() <= BOUNTY_DEATH_PERCENTAGE
		) {
			final var b = new Bounty(server, globalDifficulty, playerDifficulty);
			bounties.put(uuid, b);
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

	public static Component getBountiesMessage() {
		final var txt = Component.empty().append(Bounty.getBountyHeader());
		if (bounties.values().stream().noneMatch(Bounty::isEnabled)) {
			txt.append("Currently, there are no bounties.\n");
			txt.append(Bounty.getBountyFooter());
			return txt;
		}
		txt.append("Bounties:\n");
		get().forEach(bounty -> {
			if (!bounty.isEnabled()) return;
			final var name = Component.empty().withStyle(Constants.COLOR_DANGER).append(bounty.getPlayer().getDisplayName());
			txt.append("- ").append(name).append("\n");
		});
		txt.append(Bounty.getBountyFooter());
		return txt;
	}

	private static Component getBountyHeader() {
		final var txt = Component.empty();
		txt.append(Component.literal("==================== ").withStyle(Constants.COLOR_SEPARATOR));
		txt.append("Bounty!");
		return txt.append(Component.literal(" ====================\n").withStyle(Constants.COLOR_SEPARATOR));
	}

	private static Component getBountyFooter() {
		return Component.literal("===============================================").withStyle(Constants.COLOR_SEPARATOR);
	}

	private void bountyBroadcast() {
		final var txt = Component.empty().append(getBountyHeader());
		txt.append("A bounty is put on ");
		txt.append(Component.empty().append(player.getDisplayName()).withStyle(Constants.COLOR_DANGER));
		txt.append(" because they ");
		if (playerDifficulty.getTotalOfDeath() == 0) txt.append("never died!");
		else if (playerDifficulty.getTotalOfDeath() == 1) txt.append("died only once!");
		else txt.append(String.format("died only %d times!", playerDifficulty.getTotalOfDeath()));

		txt.append("\n\n");
		txt.append("If you kill ");
		txt.append(Component.empty().append(player.getDisplayName()).withStyle(Constants.COLOR_DANGER));
		txt.append(", you will swap your personal difficulty and gain a bonus heart!\n\n");
		txt.append("Good luck!\n");
		txt.append(getBountyFooter());

		player.level().getServer().getPlayerList().broadcastSystemMessage(txt, false);
	}

	public void onKill(PlayerDifficultyManager attackerDifficulty) {
		if (!enabled) return;
		enabled = false;
		final var attacker = attackerDifficulty.player.orElseThrow();
		final var txt = Component.empty().append(getBountyHeader());
		txt.append(Component.empty().append(attacker.getDisplayName()).withStyle(Constants.COLOR_WARNING));
		txt.append(" killed ");
		txt.append(Component.empty().append(player.getDisplayName()).withStyle(Constants.COLOR_DANGER));
		txt.append("!\n");
		txt.append(getBountyFooter());

		player.level().getServer().getPlayerList().broadcastSystemMessage(txt, false);

		final var n = attackerDifficulty.getNumberOfDeath();
		attackerDifficulty.setNumberOfDeath(playerDifficulty.getNumberOfDeath());
		playerDifficulty.setNumberOfDeath(n);

		attackerDifficulty.increaseBonusHearts();
		playerDifficulty.resetBonusHearts();

		ModCriteria.BOUNTY.trigger(player, AdvancementProvider.BOUNTY_KILLED);
		ModCriteria.BOUNTY.trigger(attacker, AdvancementProvider.BOUNTY_KILLER);
	}

	public void onDeath() {
		if (!enabled) return;
		player = playerDifficulty.player.orElseThrow();
		if ((double) playerDifficulty.getTotalOfDeath() / globalDifficulty.getTotalOfDeath() > BOUNTY_DEATH_PERCENTAGE)
			enabled = false;
	}

	public void onDisconnect() {
		if (!enabled) {
			if (task.isRunning()) task.cancel();
			return;
		}
		enabled = false;
		final var txt = Component.empty().append(getBountyHeader());
		txt.append(Component.empty().append(player.getDisplayName()).withStyle(Constants.COLOR_DANGER));
		txt.append(" disconnected. The bounty will be back when they are next connected!\n");
		txt.append(getBountyFooter());

		player.level().getServer().getPlayerList().broadcastSystemMessage(txt, false);
	}

	public ServerPlayer getPlayer() {
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
