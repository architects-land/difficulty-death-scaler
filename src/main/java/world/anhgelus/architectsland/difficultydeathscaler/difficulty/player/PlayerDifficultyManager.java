package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
		new Step(0, (_, _, updater) -> {
			updater.getModifier(HealthModifier.class).update(0);
//                updater.getModifier(LuckModifier.class).update(0.1);
			updater.getModifier(BlockBreakSpeedModifier.class).update(0.4); // is haste 2
			updater.getModifier(MovementSpeedModifier.class).update(0.1); // is speed 1
			updater.getModifier(WaypointTransmitModifier.class).update(WaypointTransmitModifier.BASE_VALUE);
		}),
		new Step(1, (_, _, updater) -> {
//                updater.getModifier(LuckModifier.class).update(0);
			updater.getModifier(BlockBreakSpeedModifier.class).update(0.2); // is haste 1
			updater.getModifier(MovementSpeedModifier.class).update(0);
			updater.getModifier(WaypointTransmitModifier.class).update(15000);
		}),
		new Step(2, (_, _, updater) -> {
			updater.getModifier(HealthModifier.class).update(-2);
			updater.getModifier(WaypointTransmitModifier.class).update(10000);
		}),
		new Step(3, (_, _, updater) -> {
			updater.getModifier(BlockBreakSpeedModifier.class).update(0);
			updater.getModifier(HealthModifier.class).update(-4);
			updater.getModifier(WaypointTransmitModifier.class).update(7500);
		}),
		new Step(5, (_, _, updater) -> {
			updater.getModifier(HealthModifier.class).update(-6);
			updater.getModifier(WaypointTransmitModifier.class).update(5000);
		}),
		new Step(7, (_, _, updater) -> {
			updater.getModifier(MovementSpeedModifier.class).update(-0.1); // is slowness 1
		}),
		new Step(8, (_, _, updater) -> {
			updater.getModifier(BlockBreakSpeedModifier.class).update(-0.2); // is mining fatigue 1
//                updater.getModifier(LuckModifier.class).update(-0.2);
			updater.getModifier(WaypointTransmitModifier.class).update(2500);
		}),
		new Step(10, (_, _, updater) ->
			updater.getModifier(HealthModifier.class).update(-8)),
		new Step(12, (_, _, updater) -> {
			updater.getModifier(MovementSpeedModifier.class).update(-0.2); // is slowness 2
			updater.getModifier(WaypointTransmitModifier.class).update(1000);
		}),
		new Step(15, (_, _, updater) ->
			updater.getModifier(HealthModifier.class).update(-10)),
	};
	public static final Identifier BONUS_HEARTS_ID = DifficultyDeathScaler.id("bonus_hearts");
	private final GlobalDifficultyManager globalManager;
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public Optional<ServerPlayer> player;
	protected double healthModifier;
	//    protected double luckModifier = 0;
	protected double blockBreakSpeedModifier;
	protected double movementSpeedModifier;
	protected double waypointTransmitModifier;

	public PlayerDifficultyManager(
		MinecraftServer server,
		GlobalDifficultyManager globalManager,
		DifficultyState.PlayerData data
	) {
		// required, otherwise this value is null
		player = Optional.empty();
		super(server, STEPS, SECONDS_BEFORE_DECREASED, data);
		this.globalManager = globalManager;
	}

	@Override
	protected void onUpdate(UpdateType updateType, DifficultyUpdater updater, int before) {
		player.ifPresent(player -> {
			applyModifiers();
			ModCriteria.REACH_PLAYER_DIFFICULTY.trigger(player, data.deaths());
			if (updateType == UpdateType.DECREASE && before >= 15)
				ModCriteria.ARBITRARY.trigger(player, AdvancementProvider.LEAVE_NO_RETURN_PLAYER);

			player.sendSystemMessage(Component.translationArg(
				generateDifficultyUpdate(updateType, updater.getDifficulty())
			), false);

			playSoundUpdate(updateType, player);
		});
	}

	@Override
	protected void onDeath(UpdateType updateType, DifficultyUpdater updater) {
		super.onDeath(updateType, updater);

		final var p = player.orElseThrow();
		newDeathDay(24 * 60 * 60 + System.currentTimeMillis() / 1000);
		if (!diedTooMuch()) return;
		// temp ban
		DifficultyDeathScaler.LOGGER.info(
			"{} has skill issue: banned for 12 hours",
			p.getName().getString()
		);
		kickIfDiedTooMuch();
		// resetting death day
		resetDeathDay();
	}

	public void newDeathDay(long endTime) {
		final var diff = endTime - System.currentTimeMillis() / 1000; // time passed
		if (diff <= 0) {
			DifficultyDeathScaler.LOGGER.warn("Death day is already passed, skipping");
			return;
		}
		data.addDeathDayEnd(endTime);
	}

	@Override
	protected void updateModifiersValue(List<Modifier<?>> modifiers) {
		modifiers.forEach(m -> {
			switch (m) {
				case final HealthModifier mod -> healthModifier = mod.getValue();
				case final BlockBreakSpeedModifier mod -> blockBreakSpeedModifier = mod.getValue();
				case final MovementSpeedModifier mod -> movementSpeedModifier = mod.getValue();
				case final WaypointTransmitModifier mod ->
					waypointTransmitModifier = mod.getValue();
				default -> throw new IllegalStateException("Unexpected value: " + m);
			}
		});
		applyBonusHearts();
	}

	private void applyBonusHearts() {
		if (player.isEmpty()) return;
		Modifier.apply(BONUS_HEARTS_ID, Attributes.MAX_HEALTH,
			AttributeModifier.Operation.ADD_VALUE, player.get(), data.bonusHearts() * 2);
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
	protected @NotNull Component generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
		final var heartAmount =
			(20 + healthModifier + globalManager.getHealthModifier()) / 2 + data.bonusHearts();

		final var txt = Component.empty();
		txt.append(generateHeaderUpdate(updateType));

		final var deathDayEnd = data.deathDayEnd();
		final var deathDay = deathDayEnd.size();

		final int deathBefore = server.getGameRules()
			.get(DifficultyDeathScaler.DEATH_BEFORE_TEMP_BAN);

		if (deathDay != 0) {
			txt.append("You died ");
			final var t = Component.empty();
			if (deathDay >= 0.8f * deathBefore) {
				t.withStyle(Constants.COLOR_DANGER);
			} else if (deathDay >= 0.4f * deathBefore) {
				t.withStyle(Constants.COLOR_WARNING);
			} else {
				t.withStyle(Constants.COLOR_OK);
			}
			t.append(String.format("%d", deathDay));
			txt.append(t).append(" time");
			if (deathDay > 1) {
				txt.append("s");
			}
			txt.append(" in 24 hours. You will lose one of these in ");
			txt.append(Component.literal(
				formatSecondsBig(deathDayEnd.getFirst() - System.currentTimeMillis() / 1000)
			).withStyle(Constants.COLOR_TIME));
			txt.append(".\n");
		}
		txt.append("\n");

		txt.append("Max hearts: ");
		final var t = Component.empty();
		if (heartAmount == 10) {
			t.withStyle(Constants.COLOR_OK);
		} else if (heartAmount >= 8) {
			t.withStyle(Constants.COLOR_WARNING);
		} else {
			t.withStyle(Constants.COLOR_DANGER);
		}
		t.append(String.format("%.0f ❤", heartAmount));
		txt.append(t).append("\n\n");

		txt.append(generateFooterUpdate(STEPS, "you didn't die", updateType));

		return txt;
	}

	@Override
	@Deprecated
	public void applyModifiers(ServerPlayer player) {
		applyModifiers();
	}

	public void applyModifiers() {
		if (player.isEmpty()) return;
		final var p = player.get();
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

	private void resetDeathDay() {
		data = data.deathDayEnd(new ArrayList<>());
	}

	public boolean diedTooMuch() {
		final var rules = server.getGameRules();
		return rules.get(DifficultyDeathScaler.ENABLE_TEMP_BAN) &&
			data.deathDayEnd().size() >= rules.get(DifficultyDeathScaler.DEATH_BEFORE_TEMP_BAN);
	}

	/**
	 * @throws IllegalStateException if the player is not temp banned
	 */
	public Component getKickedDiedTooMuchMessage() {
		return MutableComponent.create(new PlainTextContents.LiteralContents("You died too much during 24h...\nYou can log back in 12h."));
	}

	/**
	 * Kick the player if he died too much
	 *
	 * @return true if the player was kicked
	 */
	public boolean kickIfDiedTooMuch() {
		final var p = player.orElseThrow();
		if (!diedTooMuch()) return false;
		final var time = LocalDateTime.now();
		final var now = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
		final var expiry = Date.from(time.plusHours(server.getGameRules()
				.get(DifficultyDeathScaler.TEMP_BAN_DURATION))
			.atZone(ZoneId.systemDefault()).toInstant());
		final var banEntry = new UserBanListEntry(p.nameAndId(), now, "Difficulty Death Scaler", expiry,
			"You died too much during 24h...");
		server.getPlayerList().getBans().add(banEntry);
		p.connection.disconnect(getKickedDiedTooMuchMessage());
		return true;
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();
		sb.append("PlayerDifficultyManager(uuid=");
		player.ifPresent(player -> sb.append(player.getStringUUID()));
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

		public static void apply(ServerPlayer player, double value) {
			apply(ID, ATTRIBUTE, OPERATION, player, value);
		}
	}
}
