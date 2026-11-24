package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.AdvancementProvider;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyState;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyUpdater;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.*;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Constants;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GlobalDifficultyManager extends DifficultyManager<DifficultyState.ServerData> {
	public static final int SECONDS_BEFORE_DECREASED = 12 * 60 * 60; // 12 hours
	public static final Step[] STEPS = new Step[]{
		new Step(0, (server, gamerules, updater) -> {
			// mobs
			gamerules.set(GameRules.SPAWN_PHANTOMS, false, server);
			gamerules.set(GameRules.FORGIVE_DEAD_PLAYERS, true, server);
			gamerules.set(GameRules.UNIVERSAL_ANGER, false, server);
			BETTER_CREEPERS = false;
			BETTER_ZOMBIES = false;
			BETTER_SKELETON = false;
			// explosion decay
			gamerules.set(GameRules.BLOCK_EXPLOSION_DROP_DECAY, false, server);
			gamerules.set(GameRules.MOB_EXPLOSION_DROP_DECAY, false, server);
			gamerules.set(GameRules.TNT_EXPLOSION_DROP_DECAY, false, server);
			// annoying
			gamerules.set(GameRules.REDUCED_DEBUG_INFO, false, server);
			gamerules.set(GameRules.LIMITED_CRAFTING, false, server);
			gamerules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 30, server);
			gamerules.set(GameRules.WATER_SOURCE_CONVERSION, true, server);
			gamerules.set(GameRules.ENDER_PEARLS_VANISH_ON_DEATH, false, server);
			// hardcore
			gamerules.set(GameRules.NATURAL_HEALTH_REGENERATION, true, server);
			updater.getModifier(HealthModifier.class).update(0);
			updater.getModifier(StepHeightModifier.class).update(0);
			updater.getModifier(SpawnReinforcementsModifier.class).update(0);
			updater.getModifier(FallDamageMultiplierModifier.class).update(0);
			updater.getModifier(WaypointReceiveModifier.class).update(WaypointReceiveModifier.BASE_VALUE);
			updater.updateDifficulty(1);
		}),
		new Step(1, (server, gamerules, updater) -> updater.updateDifficulty(2)),
		new Step(2, (server, gamerules, updater) -> {
			gamerules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 70, server);
		}),
		new Step(3, (server, gamerules, updater) -> updater.updateDifficulty(3)),
		new Step(5, (server, gamerules, updater) -> {
			gamerules.set(GameRules.SPAWN_PHANTOMS, true, server);
			gamerules.set(GameRules.BLOCK_EXPLOSION_DROP_DECAY, true, server);
			gamerules.set(GameRules.MOB_EXPLOSION_DROP_DECAY, true, server);
		}),
		new Step(7, (server, gamerules, updater) -> {
			updater.getModifier(WaypointReceiveModifier.class).update(10000);
			updater.getModifier(FollowRangeModifier.class).update(0.25);
			BETTER_SKELETON = true;
		}),
		new Step(8, (server, gamerules, updater) -> {
			updater.getModifier(FallDamageMultiplierModifier.class).update(0.25);
		}),
		new Step(10, (server, gamerules, updater) -> {
			updater.getModifier(WaypointReceiveModifier.class).update(7500);
			updater.getModifier(HealthModifier.class).update(-2);
		}),
		new Step(12, (server, gamerules, updater) -> {
			updater.getModifier(StepHeightModifier.class).update(0.6);
		}),
		new Step(13, (server, gamerules, updater) -> {
			gamerules.set(GameRules.TNT_EXPLOSION_DROP_DECAY, true, server);
			gamerules.set(GameRules.ENDER_PEARLS_VANISH_ON_DEATH, true, server);
		}),
		new Step(15, (server, gamerules, updater) -> {
			updater.getModifier(SpawnReinforcementsModifier.class).update(0.25);
			BETTER_ZOMBIES = true;
		}),
		new Step(16, (server, gamerules, updater) -> {
			updater.getModifier(WaypointReceiveModifier.class).update(5000);
			gamerules.set(GameRules.REDUCED_DEBUG_INFO, true, server);
		}),
		new Step(18, (server, gamerules, updater) -> {
			updater.getModifier(FallDamageMultiplierModifier.class).update(0.5);
		}),
		new Step(20, (server, gamerules, updater) -> {
			updater.getModifier(HealthModifier.class).update(-4);
		}),
		new Step(21, (server, gamerules, updater) -> {
			updater.getModifier(FallDamageMultiplierModifier.class).update(0.5);
		}),
		new Step(22, (server, gamerules, updater) -> {
			updater.getModifier(WaypointReceiveModifier.class).update(3000);
			gamerules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 100, server);
		}),
		new Step(23, (server, gamerules, updater) -> {
			updater.getModifier(FollowRangeModifier.class).update(0.35);
		}),
		new Step(24, (server, gamerules, updater) -> {
			gamerules.set(GameRules.WATER_SOURCE_CONVERSION, false, server);
		}),
		new Step(25, (server, gamerules, updater) -> {
			updater.getModifier(SpawnReinforcementsModifier.class).update(0.5);
		}),
		new Step(26, (server, gamerules, updater) -> {
			updater.getModifier(StepHeightModifier.class).update(1);
		}),
		new Step(28, (server, gamerules, updater) -> {
			updater.getModifier(WaypointReceiveModifier.class).update(2000);
			gamerules.set(GameRules.LIMITED_CRAFTING, true, server);
		}),
		new Step(30, (server, gamerules, updater) -> {
			gamerules.set(GameRules.UNIVERSAL_ANGER, true, server);
		}),
		new Step(32, (server, gamerules, updater) -> {
			updater.getModifier(FallDamageMultiplierModifier.class).update(1);
		}),
		new Step(33, (server, gamerules, updater) -> {
			updater.getModifier(SpawnReinforcementsModifier.class).update(0.75);
		}),
		new Step(35, (server, gamerules, updater) -> {
			updater.getModifier(FollowRangeModifier.class).update(1);
			BETTER_CREEPERS = true;
		}),
		new Step(37, (server, gamerules, updater) -> {
			gamerules.set(GameRules.FORGIVE_DEAD_PLAYERS, false, server);
		}),
		new Step(40, (server, gamerules, updater) -> {
			gamerules.set(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
		}),
	};
	public static int PIGLIN_BRUTES_BOOST = 0;
	public static boolean POLAR_NIGHT = false;
	public static EntityModifies CUSTOM_SPAWN_EFFECTS = (entity) -> {
	};
	protected static boolean BETTER_SKELETON = false;
	protected static boolean BETTER_ZOMBIES = false;
	protected static boolean BETTER_CREEPERS = false;
	private final DifficultyIncrease increaser;
	protected double healthModifier = 0;
	protected double followRangeModifier = 0;
	protected double stepHeightModifier = 0;
	protected double spawnReinforcementModifier = 0;
	protected double fallDamageMultiplierModifier = 0;
	protected double waypointReceiveModifier = 0;

	public GlobalDifficultyManager(MinecraftServer server) {
		super(server, STEPS, SECONDS_BEFORE_DECREASED);

		data = DifficultyState.get(server);

		increaser = new DifficultyIncrease(this, timer, data.timeBeforeIncrease(), data.increaseEnabled());
		// update difficulty after restart
		server.setDifficulty(getUpdater().getDifficulty(), true);
	}

	public static boolean areSkeletonsBetter() {
		return BETTER_SKELETON;
	}

	public static boolean areZombiesBetter() {
		return BETTER_ZOMBIES;
	}

	public static boolean areCreepersBetter() {
		return BETTER_CREEPERS;
	}

	@Override
	protected void onUpdate(UpdateType updateType, DifficultyUpdater updater, int before) {
		final var difficulty = POLAR_NIGHT ? updater.getDifficulty() : Difficulty.HARD;
		server.setDifficulty(difficulty, true);

		updateModifiersValue(updater);

		final var pm = server.getPlayerList();

		pm.getPlayers().forEach(p -> {
			updater.getModifiers().forEach(m -> {
				if (m instanceof final HealthModifier mod) mod.apply(p);
				else if (m instanceof final FallDamageMultiplierModifier mod) mod.apply(p);
			});
			playSoundUpdate(updateType, p);
			ModCriteria.REACH_GLOBAL_DIFFICULTY.trigger(p, data.deaths());
			if (updateType == UpdateType.DECREASE && before >= 37)
				ModCriteria.ARBITRARY.trigger(p, AdvancementProvider.LEAVE_NO_RETURN);
		});

		BossManager.onDifficultyUpdate(this);

		pm.broadcastSystemMessage(Component.translationArg(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);


		if (updateType != UpdateType.AUTOMATIC_INCREASE && updateType != UpdateType.DECREASE)
			increaser.restart();
	}

	@Override
	protected void updateModifiersValue(List<Modifier<?>> modifiers) {
		modifiers.forEach(m -> {
			if (m instanceof final HealthModifier mod) {
				healthModifier = mod.getValue();
			} else if (m instanceof final FollowRangeModifier mod) {
				followRangeModifier = mod.getValue();
			} else if (m instanceof final StepHeightModifier mod) {
				stepHeightModifier = mod.getValue();
			} else if (m instanceof final SpawnReinforcementsModifier mod) {
				spawnReinforcementModifier = mod.getValue();
			} else if (m instanceof final FallDamageMultiplierModifier mod) {
				fallDamageMultiplierModifier = mod.getValue();
			} else if (m instanceof final WaypointReceiveModifier mod) {
				waypointReceiveModifier = mod.getValue();
			}
		});
	}

	@Override
	protected @NotNull Component generateDifficultyUpdate(UpdateType updateType, net.minecraft.world.@Nullable Difficulty difficulty) {
		final var txt = Component.empty();
		txt.append(generateHeaderUpdate(updateType));
		txt.append("World Difficulty: ");
		if (difficulty == Difficulty.EASY) {
			txt.append(Component.literal("Easy").withStyle(Constants.COLOR_OK));
		} else if (difficulty == Difficulty.NORMAL) {
			txt.append(Component.literal("Normal").withStyle(Constants.COLOR_WARNING));
		} else {
			txt.append(Component.literal("Hard").withStyle(Constants.COLOR_DANGER));
		}
		if (data.deaths() >= STEPS[1].level()) {
			txt.append("\n\n");
		}
		if (data.deaths() >= STEPS[STEPS.length - 1].level()) {
			txt.append(Component.literal("Well... Good luck... you dont have regen anymore").withStyle(Constants.COLOR_DANGER));
		} else if (data.deaths() >= STEPS[21].level()) {
			txt.append(Component.literal("Nether is gonna be very dangerous").withStyle(Constants.COLOR_DANGER));
		} else if (data.deaths() >= STEPS[9].level()) {
			txt.append(Component.literal("This is so fcking annoying!").withStyle(Constants.COLOR_WARNING));
		} else if (data.deaths() >= STEPS[5].level()) {
			txt.append(Component.literal("Mobs are modified, right?...").withStyle(Constants.COLOR_WARNING));
		} else if (data.deaths() >= STEPS[3].level()) {
			txt.append(Component.literal("Normal difficulty is back!").withStyle(Constants.COLOR_OK));
		} else if (data.deaths() >= STEPS[1].level()) {
			txt.append(Component.literal("Oh no, the difficulty is becoming harder.").withStyle(Constants.COLOR_OK));
		}
		txt.append("\n\n");

		if (updateType == null && increaser.isEnabled()) updateType = UpdateType.AUTOMATIC_INCREASE;
		txt.append(generateFooterUpdate(STEPS, "no one died", updateType));

		return txt;
	}

	@Override
	public void applyModifiers(ServerPlayer player) {
		HealthModifier.apply(player, healthModifier);
		FallDamageMultiplierModifier.apply(player, fallDamageMultiplierModifier);
		WaypointReceiveModifier.apply(player, waypointReceiveModifier);
	}

	public void onEntitySpawn(Monster hostile) {
		FollowRangeModifier.apply(hostile, followRangeModifier);
		StepHeightModifier.apply(hostile, stepHeightModifier);
		if (hostile instanceof final Zombie z)
			SpawnReinforcementsModifier.apply(z, spawnReinforcementModifier);
		// if mobs was already spawned, return
		if (hostile.hasCustomName()) return;
		CUSTOM_SPAWN_EFFECTS.modify(hostile);
		if (hostile instanceof Piglin) {
			MobUtils.customSpawn(
				hostile.getRandom(),
				1.5f * (data.deaths() + PIGLIN_BRUTES_BOOST - 27),
				20 + 1.5f * PIGLIN_BRUTES_BOOST,
				() -> {
					// spawn piglin brutes
					EntityTypes.PIGLIN_BRUTE.spawn((ServerLevel) hostile.level(),
						hostile.getBlockPosBelowThatAffectsMyMovement(), EntitySpawnReason.SPAWN_ITEM_USE);
					hostile.discard();
					return null;
				});
		}
	}

	public void save(Map<UUID, PlayerDifficultyManager> players) {
		var base = save();
		base.increaseEnabled(increaser.isEnabled())
			.timeBeforeIncrease(increaser.getTickingBeforeRun());
		for (var p : players.entrySet()) {
			base.setPlayer(p.getKey(), p.getValue().save());
		}
	}

	public double getHealthModifier() {
		return healthModifier;
	}

	@Override
	public String toString() {
		return "GlobalDifficultyManager(number of death=" + data.deaths() +
			", total of death=" + data.deathsTotal() +
			", piglin brutes boost=" + PIGLIN_BRUTES_BOOST +
			") {better skeletons=" + BETTER_SKELETON +
			", better zombies=" + BETTER_ZOMBIES +
			", better creepers=" + BETTER_CREEPERS +
			", health modifier=" + healthModifier +
			", follow range modifier=" + followRangeModifier +
			", step height modifier=" + stepHeightModifier +
			", spawn reinforcement modifier=" + spawnReinforcementModifier +
			", fall damage multiplier modifier=" + fallDamageMultiplierModifier +
			"}";
	}

	@FunctionalInterface
	public interface EntityModifies {
		void modify(LivingEntity entity);
	}

	public static class HealthModifier extends PlayerHealthModifier {
		public static final Identifier ID = DifficultyDeathScaler.id("global_health");

		public HealthModifier() {
			super(ID);
		}

		public static void apply(ServerPlayer player, double value) {
			apply(ID, ATTRIBUTE, OPERATION, player, value);
		}
	}
}
