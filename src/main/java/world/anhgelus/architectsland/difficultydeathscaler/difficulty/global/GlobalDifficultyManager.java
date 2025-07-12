package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyUpdater;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.*;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Constants;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import java.util.List;

public class GlobalDifficultyManager extends DifficultyManager {
    public static final int SECONDS_BEFORE_DECREASED = 12 * 60 * 60; // 12 hours
    public static int PIGLIN_BRUTES_BOOST = 0;
    public static boolean POLAR_NIGHT = false;
    public static EntityModifies CUSTOM_SPAWN_EFFECTS = (entity) -> {
    };
    protected static boolean BETTER_SKELETON = false;
    protected static boolean BETTER_ZOMBIES = false;
    protected static boolean BETTER_CREEPERS = false;
    public static final Step[] STEPS = new Step[]{
            new Step(0, (server, gamerules, updater) -> {
                // mobs
                gamerules.get(GameRules.DO_INSOMNIA).set(false, server);
                gamerules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(true, server);
                gamerules.get(GameRules.UNIVERSAL_ANGER).set(false, server);
                BETTER_CREEPERS = false;
                BETTER_ZOMBIES = false;
                BETTER_SKELETON = false;
                // explosion decay
                gamerules.get(GameRules.BLOCK_EXPLOSION_DROP_DECAY).set(false, server);
                gamerules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(false, server);
                gamerules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(false, server);
                // annoying
                gamerules.get(GameRules.REDUCED_DEBUG_INFO).set(false, server);
                gamerules.get(GameRules.DO_LIMITED_CRAFTING).set(false, server);
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(30, server);
                gamerules.get(GameRules.WATER_SOURCE_CONVERSION).set(true, server);
                gamerules.get(GameRules.ENDER_PEARLS_VANISH_ON_DEATH).set(false, server);
                // hardcore
                gamerules.get(GameRules.NATURAL_REGENERATION).set(true, server);
                updater.getModifier(HealthModifier.class).update(0);
                updater.getModifier(StepHeightModifier.class).update(0);
                updater.getModifier(SpawnReinforcementsModifier.class).update(0);
                updater.getModifier(FallDamageMultiplierModifier.class).update(0);
                updater.getModifier(WaypointReceiveModifier.class).update(WaypointReceiveModifier.BASE_VALUE);
                updater.updateDifficulty(1);
            }),
            new Step(1, (server, gamerules, updater) -> updater.updateDifficulty(2)),
            new Step(2, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(70, server);
            }),
            new Step(3, (server, gamerules, updater) -> updater.updateDifficulty(3)),
            new Step(5, (server, gamerules, updater) -> {
                gamerules.get(GameRules.DO_INSOMNIA).set(true, server);
                gamerules.get(GameRules.BLOCK_EXPLOSION_DROP_DECAY).set(true, server);
                gamerules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(true, server);
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
                gamerules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(true, server);
                gamerules.get(GameRules.ENDER_PEARLS_VANISH_ON_DEATH).set(true, server);
            }),
            new Step(15, (server, gamerules, updater) -> {
                updater.getModifier(SpawnReinforcementsModifier.class).update(0.25);
                BETTER_ZOMBIES = true;
            }),
            new Step(16, (server, gamerules, updater) -> {
                updater.getModifier(WaypointReceiveModifier.class).update(5000);
                gamerules.get(GameRules.REDUCED_DEBUG_INFO).set(true, server);
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
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(100, server);
            }),
            new Step(23, (server, gamerules, updater) -> {
                updater.getModifier(FollowRangeModifier.class).update(0.35);
            }),
            new Step(24, (server, gamerules, updater) -> {
                gamerules.get(GameRules.WATER_SOURCE_CONVERSION).set(false, server);
            }),
            new Step(25, (server, gamerules, updater) -> {
                updater.getModifier(SpawnReinforcementsModifier.class).update(0.5);
            }),
            new Step(26, (server, gamerules, updater) -> {
                updater.getModifier(StepHeightModifier.class).update(1);
            }),
            new Step(28, (server, gamerules, updater) -> {
                updater.getModifier(WaypointReceiveModifier.class).update(2000);
                gamerules.get(GameRules.DO_LIMITED_CRAFTING).set(true, server);
            }),
            new Step(30, (server, gamerules, updater) -> {
                gamerules.get(GameRules.UNIVERSAL_ANGER).set(true, server);
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
                gamerules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(false, server);
            }),
            new Step(40, (server, gamerules, updater) -> {
                gamerules.get(GameRules.NATURAL_REGENERATION).set(false, server);
            }),
    };
    private final DifficultyIncrease increaser;
    protected double healthModifier = 0;
    protected double followRangeModifier = 0;
    protected double stepHeightModifier = 0;
    protected double spawnReinforcementModifier = 0;
    protected double fallDamageMultiplierModifier = 0;

    public GlobalDifficultyManager(MinecraftServer server) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        // get state
        DifficultyDeathScaler.LOGGER.info("Loading global difficulty data");
        final var state = StateSaver.getServerState(server);
        // load data
        increaser = new DifficultyIncrease(this, timer, state.difficulty.timeBeforeIncrease, state.difficulty.increaseEnabled);
        load(state.difficulty);
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
    protected void onUpdate(UpdateType updateType, DifficultyUpdater updater) {
        final var difficulty = POLAR_NIGHT ? updater.getDifficulty() : Difficulty.HARD;
        server.setDifficulty(difficulty, true);

        updateModifiersValue(updater);

        final var pm = server.getPlayerManager();

        pm.getPlayerList().forEach(p -> {
            updater.getModifiers().forEach(m -> {
                if (m instanceof final HealthModifier mod) mod.apply(p);
                else if (m instanceof final FallDamageMultiplierModifier mod) mod.apply(p);
            });
            playSoundUpdate(updateType, p);
            ModCriteria.REACH_GLOBAL_DIFFICULTY.trigger(p, numberOfDeath);
        });

        BossManager.onDifficultyUpdate(this);

        pm.broadcast(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);


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
            }
        });
    }

    @Override
    protected @NotNull Text generateDifficultyUpdate(UpdateType updateType, net.minecraft.world.@Nullable Difficulty difficulty) {
        final var txt = Text.empty();
        txt.append(generateHeaderUpdate(updateType));
        txt.append("World Difficulty: ");
        if (difficulty == Difficulty.EASY) {
            txt.append(Text.literal("Easy").formatted(Constants.COLOR_OK));
        } else if (difficulty == Difficulty.NORMAL) {
            txt.append(Text.literal("Normal").formatted(Constants.COLOR_WARNING));
        } else {
            txt.append(Text.literal("Hard").formatted(Constants.COLOR_DANGER));
        }
        if (numberOfDeath >= STEPS[1].level()) {
            txt.append("\n\n");
        }
        if (numberOfDeath >= STEPS[STEPS.length - 1].level()) {
            txt.append(Text.literal("Well... Good luck... you dont have regen anymore").formatted(Constants.COLOR_DANGER));
        } else if (numberOfDeath >= STEPS[21].level()) {
            txt.append(Text.literal("Nether is gonna be very dangerous").formatted(Constants.COLOR_DANGER));
        } else if (numberOfDeath >= STEPS[9].level()) {
            txt.append(Text.literal("This is so fcking annoying!").formatted(Constants.COLOR_WARNING));
        } else if (numberOfDeath >= STEPS[5].level()) {
            txt.append(Text.literal("Mobs are modified, right?...").formatted(Constants.COLOR_WARNING));
        } else if (numberOfDeath >= STEPS[3].level()) {
            txt.append(Text.literal("Normal difficulty is back!").formatted(Constants.COLOR_OK));
        } else if (numberOfDeath >= STEPS[1].level()) {
            txt.append(Text.literal("Oh no, the difficulty is becoming harder.").formatted(Constants.COLOR_OK));
        }
        txt.append("\n\n");

        if (updateType == null && increaser.isEnabled()) updateType = UpdateType.AUTOMATIC_INCREASE;
        txt.append(generateFooterUpdate(STEPS, "no one died", updateType));

        return txt;
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        HealthModifier.apply(player, healthModifier);
        FallDamageMultiplierModifier.apply(player, fallDamageMultiplierModifier);
    }

    public void onEntitySpawn(HostileEntity hostile) {
        FollowRangeModifier.apply(hostile, followRangeModifier);
        StepHeightModifier.apply(hostile, stepHeightModifier);
        if (hostile instanceof final ZombieEntity z) SpawnReinforcementsModifier.apply(z, spawnReinforcementModifier);
        // if mobs was already spawned, return
        if (hostile.hasCustomName()) return;
        CUSTOM_SPAWN_EFFECTS.modify(hostile);
        if (hostile instanceof PiglinEntity) {
            MobUtils.customSpawn(
                    hostile.getRandom(),
                    1.5f * (numberOfDeath + PIGLIN_BRUTES_BOOST - 27),
                    20 + 1.5f * PIGLIN_BRUTES_BOOST,
                    () -> {
                        // spawn piglin brutes
                        EntityType.PIGLIN_BRUTE.spawn((ServerWorld) hostile.getWorld(), hostile.getBlockPos(), SpawnReason.MOB_SUMMONED);
                        hostile.discard();
                        return null;
                    });
        }
    }

    public void save() {
        // get state
        DifficultyDeathScaler.LOGGER.info("Saving global difficulty data");
        final var state = StateSaver.getServerState(server);
        // save state
        save(state.difficulty);

        state.difficulty.timeBeforeIncrease = DifficultyIncrease.SECONDS_BEFORE_INCREASE * 20 - increaser.getTickingBeforeRun();
        state.difficulty.increaseEnabled = increaser.isEnabled();
    }

    public double getHealthModifier() {
        return healthModifier;
    }

    @Override
    public String toString() {
        String sb = "GlobalDifficultyManager(number of death=" + numberOfDeath +
                ", total of death=" + totalOfDeath +
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
        return sb;
    }

    @FunctionalInterface
    public interface EntityModifies {
        void modify(LivingEntity entity);
    }

    public static class HealthModifier extends PlayerHealthModifier {
        public static final Identifier ID = Identifier.of(PREFIX + "global_health_modifier");

        public HealthModifier() {
            super(ID);
        }

        public static void apply(ServerPlayerEntity player, double value) {
            apply(ID, ATTRIBUTE, OPERATION, player, value);
        }
    }
}
