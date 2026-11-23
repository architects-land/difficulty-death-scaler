package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.AdvancementProvider;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyState;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyUpdater;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.*;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Constants;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class PlayerDifficultyManager extends DifficultyManager<DifficultyState.PlayerData> {
	public static final int SECONDS_BEFORE_DECREASED = 24 * 60 * 60;
	public static final Step[] STEPS = new Step[]{
		new Step(0, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(0);
//                updater.getModifier(LuckModifier.class).update(0.1);
			updater.getModifier(BlockBreakSpeedModifier.class).update(0.4); // is haste 2
			updater.getModifier(MovementSpeedModifier.class).update(0.1); // is speed 1
			updater.getModifier(WaypointTransmitModifier.class).update(WaypointTransmitModifier.BASE_VALUE);
		}),
		new Step(1, (server, gamerules, updater) -> {
//                updater.getModifier(LuckModifier.class).update(0);
			updater.getModifier(BlockBreakSpeedModifier.class).update(0.2); // is haste 1
			updater.getModifier(MovementSpeedModifier.class).update(0);
			updater.getModifier(WaypointTransmitModifier.class).update(15000);
		}),
		new Step(2, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(-2);
			updater.getModifier(WaypointTransmitModifier.class).update(10000);
		}),
		new Step(3, (server, gamerules, updater) -> {
			updater.getModifier(BlockBreakSpeedModifier.class).update(0);
			updater.getModifier(HealthModifier.class).update(-4);
			updater.getModifier(WaypointTransmitModifier.class).update(7500);
		}),
		new Step(5, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(-6);
			updater.getModifier(WaypointTransmitModifier.class).update(5000);
		}),
		new Step(7, (server, gamerules, updater) -> {
			updater.getModifier(MovementSpeedModifier.class).update(-0.1); // is slowness 1
		}),
		new Step(8, (server, gamerules, updater) -> {
			updater.getModifier(BlockBreakSpeedModifier.class).update(-0.2); // is mining fatigue 1
//                updater.getModifier(LuckModifier.class).update(-0.2);
			updater.getModifier(WaypointTransmitModifier.class).update(2500);
		}),
		new Step(10, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(-8);
		}),
		new Step(12, (server, gamerules, updater) -> {
			updater.getModifier(MovementSpeedModifier.class).update(-0.2); // is slowness 2
			updater.getModifier(WaypointTransmitModifier.class).update(1000);
		}),
		new Step(15, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(-10);
		}),
	};
	public static final Identifier BONUS_HEARTS_ID = Identifier.of(Modifier.PREFIX + "bonus_hearts_modifier");
	public static boolean ENABLE_TEMP_BAN = true;
	public static int DEATH_BEFORE_TEMP_BAN = 5;
	public static int TEMP_BAN_DURATION = 12;
	private final GlobalDifficultyManager globalManager;
	private final List<TickTask> deathDayTasks = new ArrayList<>();
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Optional<ServerPlayerEntity> player = Optional.empty();
	protected double healthModifier = 0;
	//    protected double luckModifier = 0;
	protected double blockBreakSpeedModifier = 0;
	protected double movementSpeedModifier = 0;
	protected double waypointTransmitModifier = 0;

	public PlayerDifficultyManager(
		MinecraftServer server,
		GlobalDifficultyManager globalManager,
		DifficultyState.PlayerData data
	) {
		super(server, STEPS, SECONDS_BEFORE_DECREASED);
		this.globalManager = globalManager;

		this.data = data;
		delayFirstTask(data.timeBeforeReduce());

		for (final var timeEnd : data.deathDayEnd()) {
			try {
				newDeathDay(timeEnd);
			} catch (IllegalArgumentException e) {
				DifficultyDeathScaler.LOGGER.error("An error occurred while loading data", e);
				DifficultyDeathScaler.LOGGER.warn("Removing one day death");
			}
		}
	}

	@Override
	protected void onUpdate(UpdateType updateType, DifficultyUpdater updater, int before) {
		updateModifiersValue(updater);
		player.ifPresent(player -> {
			ModCriteria.REACH_PLAYER_DIFFICULTY.trigger(player, data.deaths());
			if (updateType == UpdateType.DECREASE && before >= 15)
				ModCriteria.ARBITRARY.trigger(player, AdvancementProvider.LEAVE_NO_RETURN_PLAYER);

			player.sendMessage(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);

			playSoundUpdate(updateType, player);
		});
	}

	@Override
	protected void onDeath(UpdateType updateType, DifficultyUpdater updater) {
		super.onDeath(updateType, updater);

		player.ifPresentOrElse(player -> {
			if (player.getWorld().isClient()) return;
			newDeathDay(24 * 60 * 60 + System.currentTimeMillis() / 1000);
			if (!diedTooMuch()) return;
			// temp ban
			DifficultyDeathScaler.LOGGER.info(
				"{} has skill issue: banned for 12 hours",
				player.getName().getString()
			);
			kickIfDiedTooMuch();
			// resetting death day
			resetDeathDay();
		}, () -> {
			DifficultyDeathScaler.LOGGER.error("Updating death of null player. UpdateType {}", updateType);
			throw new IllegalStateException("Player is null");
		});
	}

	public void newDeathDay(long endTime) {
		final var diff = endTime - System.currentTimeMillis() / 1000; // time passed
		if (diff <= 0) {
			DifficultyDeathScaler.LOGGER.info("Death day is already passed, skipping");
			return;
		}
		data.addDeathDayEnd(endTime);
		scheduleDeathDayTask(diff * 20);
	}

	@Override
	protected void updateModifiersValue(List<Modifier<?>> modifiers) {
		modifiers.forEach(m -> {
			if (m instanceof final HealthModifier mod) {
				healthModifier = mod.getValue();
				player.ifPresent(mod::apply);
			}/* else if (m instanceof final LuckModifier mod) {
                luckModifier = mod.getValue();
                mod.apply(player);
            } */ else if (m instanceof final BlockBreakSpeedModifier mod) {
				blockBreakSpeedModifier = mod.getValue();
				player.ifPresent(mod::apply);
			} else if (m instanceof final MovementSpeedModifier mod) {
				movementSpeedModifier = mod.getValue();
				player.ifPresent(mod::apply);
			} else if (m instanceof final WaypointTransmitModifier mod) {
				waypointTransmitModifier = mod.getValue();
				player.ifPresent(mod::apply);
			}
		});
		applyBonusHearts();
	}

	private void applyBonusHearts() {
		if (player.isEmpty()) return;
		Modifier.apply(BONUS_HEARTS_ID, EntityAttributes.MAX_HEALTH,
			EntityAttributeModifier.Operation.ADD_VALUE, player.get(), data.bonusHearts() * 2);
	}

	public void increaseBonusHearts() {
		data = data.increaseBonusHearts();
		applyBonusHearts();
	}

	public void resetBonusHearts() {
		data = data.bonusHearts(0);
		applyBonusHearts();
	}

	@Override
	protected @NotNull Text generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
		final var heartAmount =
			(20 + healthModifier + globalManager.getHealthModifier()) / 2 + data.bonusHearts();

		final var txt = Text.empty();
		txt.append(generateHeaderUpdate(updateType));

		final var deathDay = data.deathDayEnd().size();

		if (deathDay != 0) {
			txt.append("You died ");
			final var t = Text.empty();
			if (deathDay >= 0.8f * DEATH_BEFORE_TEMP_BAN) {
				t.formatted(Constants.COLOR_DANGER);
			} else if (deathDay >= 0.4f * DEATH_BEFORE_TEMP_BAN) {
				t.formatted(Constants.COLOR_WARNING);
			} else {
				t.formatted(Constants.COLOR_OK);
			}
			t.append(String.format("%d", deathDay));
			txt.append(t).append(" time");
			if (deathDay > 1) {
				txt.append("s");
			}
			txt.append(" in 24 hours. You will lose one of these in ");
			txt.append(Text.literal(formatSecondsBig(data.deathDayEnd().getFirst() - System.currentTimeMillis() / 1000)).formatted(Constants.COLOR_TIME));
			txt.append(".\n");
		}
		txt.append("\n");

		txt.append("Max hearts: ");
		final var t = Text.empty();
		if (heartAmount == 10) {
			t.formatted(Constants.COLOR_OK);
		} else if (heartAmount >= 8) {
			t.formatted(Constants.COLOR_WARNING);
		} else {
			t.formatted(Constants.COLOR_DANGER);
		}
		t.append(String.format("%.0f ❤", heartAmount));
		txt.append(t).append("\n\n");

		txt.append(generateFooterUpdate(STEPS, "you didn't die", updateType));

		return txt;
	}

	@Override
	public void applyModifiers(ServerPlayerEntity player) {
		applyModifiers();
	}

	public void applyModifiers() {
		assert player.isPresent();
		final var p = player.orElseThrow();
		HealthModifier.apply(p, healthModifier);
//        LuckModifier.apply(player, luckModifier);
		BlockBreakSpeedModifier.apply(p, blockBreakSpeedModifier);
		MovementSpeedModifier.apply(p, movementSpeedModifier);
		WaypointTransmitModifier.apply(p, waypointTransmitModifier);
		applyBonusHearts();
	}

	public void setDeathDay(int n) {
		final var deathDay = data.deathDayEnd().size();
		if (deathDay == n) return;
		final var t = 20 * 60 * 60 + System.currentTimeMillis() / 1000;
		if (n > deathDay) {
			for (int i = 0; i < n - deathDay; i++) newDeathDay(t);
		} else {
			resetDeathDay();
			for (int i = 0; i < n; i++) newDeathDay(t);
		}
		kickIfDiedTooMuch();
	}

	private void scheduleDeathDayTask(long ticksDelay) {
		final var task = new TickTask(deathDayTasks::removeFirst, ticksDelay);
		timer.dds_runTask(task);
		deathDayTasks.add(task);
	}

	private void resetDeathDay() {
		deathDayTasks.forEach(TickTask::cancel);
		deathDayTasks.clear();
		data = data.deathDayEnd(new ArrayList<>());
	}

	public boolean diedTooMuch() {
		return ENABLE_TEMP_BAN && data.deathDayEnd().size() >= DEATH_BEFORE_TEMP_BAN;
	}

	/**
	 * @throws IllegalStateException if the player is not temp banned
	 */
	public Text getKickedDiedTooMuchMessage() {
		return MutableText.of(new PlainTextContent.Literal("You died too much during 24h...\nYou can log back in 12h."));
	}

	/**
	 * Kick the player if he died too much
	 *
	 * @return true if the player was kicked
	 */
	public boolean kickIfDiedTooMuch() {
		final var p = player.orElseThrow();
		final var server = p.getServer();
		if (server == null) {
			DifficultyDeathScaler.LOGGER.warn("Server is null");
			return false;
		}
		if (!diedTooMuch()) return false;
		final var time = LocalDateTime.now();
		final var now = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
		final var expiry = Date.from(time.plusHours(TEMP_BAN_DURATION).atZone(ZoneId.systemDefault()).toInstant());
		final var banEntry = new BannedPlayerEntry(p.getGameProfile(), now, "Difficulty Death Scaler", expiry, "You died too much during 24h...");
		server.getPlayerManager().getUserBanList().add(banEntry);
		p.networkHandler.disconnect(getKickedDiedTooMuchMessage());
		return true;
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();
		sb.append("PlayerDifficultyManager(uuid=");
		player.ifPresent(player -> sb.append(player.getUuidAsString()));
		sb.append(", number of death=")
			.append(data.deaths())
			.append(", total of death=")
			.append(data.deathsTotal())
			.append(", death day=")
			.append(data.deathDayEnd().size())
			.append(") {health modifier=")
			.append(healthModifier)
			.append(", bonus hearts=")
			.append(data.bonusHearts())
//                .append(", luck modifier=")
//                .append(luckModifier)
			.append(", block break speed modifier=")
			.append(blockBreakSpeedModifier)
			.append(", movement speed modifier=")
			.append(movementSpeedModifier)
			.append("}");
		return sb.toString();
	}

	public static class HealthModifier extends PlayerHealthModifier {
		public static final Identifier ID =
			DifficultyDeathScaler.id("player_health_modifier");

		public HealthModifier() {
			super(ID);
		}

		public static void apply(ServerPlayerEntity player, double value) {
			apply(ID, ATTRIBUTE, OPERATION, player, value);
		}
	}
}
