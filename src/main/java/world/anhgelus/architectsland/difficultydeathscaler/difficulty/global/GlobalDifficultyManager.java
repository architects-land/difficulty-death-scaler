package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinEntity;
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
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.*;

import java.util.List;

public class GlobalDifficultyManager extends DifficultyManager {
    public static final int SECONDS_BEFORE_DECREASED = 12*60*60; // 12 hours

    private final DifficultyIncrease increaser; // 12 hours

    public static class HealthModifier extends PlayerHealthModifier {
        public static final Identifier ID = Identifier.of(PREFIX + "global_health_modifier");

        public HealthModifier() {
            super(ID);
        }

        public static void apply(ServerPlayerEntity player, double value) {
            apply(ID, ATTRIBUTE, OPERATION, player, value);
        }
    }

    public static final Step[] STEPS = new Step[]{
            new Step(0, (server, gamerules, updater) -> {
                // mobs
                gamerules.get(GameRules.DO_INSOMNIA).set(false, server);
                gamerules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(true, server);
                gamerules.get(GameRules.UNIVERSAL_ANGER).set(false, server);
                BETTER_CREEPERS = false;
                BETTER_ZOMBIES = false;
                BETTER_SKELETON = false;
                SPAWN_PIGLIN_BRUTE = false;
                // explosion decay
                gamerules.get(GameRules.BLOCK_EXPLOSION_DROP_DECAY).set(false, server);
                gamerules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(false, server);
                gamerules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(false, server);
                // annoying
                gamerules.get(GameRules.REDUCED_DEBUG_INFO).set(false, server);
                gamerules.get(GameRules.DO_LIMITED_CRAFTING).set(false, server);
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(30, server);
                gamerules.get(GameRules.WATER_SOURCE_CONVERSION).set(true, server);
                // hardcore
                gamerules.get(GameRules.NATURAL_REGENERATION).set(true, server);
                updater.getModifier(HealthModifier.class).update(0);
                updater.getModifier(StepHeightModifier.class).update(0);
                updater.getModifier(SpawnReinforcementsModifier.class).update(0);
                updater.getModifier(FallDamageMultiplierModifier.class).update(0);
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
                updater.getModifier(FollowRangeModifier.class).update(0.25);
                BETTER_SKELETON = true;
            }),
            new Step(8, (server, gamerules, updater) -> {
                updater.getModifier(FallDamageMultiplierModifier.class).update(0.25);
            }),
            new Step(10, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-2);
            }),
            new Step(12, (server, gamerules, updater) -> {
                updater.getModifier(StepHeightModifier.class).update(0.6);
            }),
            new Step(13, (server, gamerules, updater) -> {
                gamerules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(true, server);
            }),
            new Step(15, (server, gamerules, updater) -> {
                updater.getModifier(SpawnReinforcementsModifier.class).update(0.25);
                BETTER_ZOMBIES = true;
            }),
            new Step(16, (server, gamerules, updater) -> {
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
                gamerules.get(GameRules.DO_LIMITED_CRAFTING).set(true, server);
                SPAWN_PIGLIN_BRUTE = true;
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

    protected static boolean BETTER_SKELETON = false;
    protected static boolean BETTER_ZOMBIES = false;
    protected static boolean BETTER_CREEPERS = false;
    protected static boolean SPAWN_PIGLIN_BRUTE = false;

    protected double healthModifier = 0;
    protected double followRangeModifier = 0;
    protected double stepHeightModifier = 0;
    protected double spawnReinforcementModifier = 0;
    protected double fallDamageMultiplierModifier = 0;

    private int totalOfDeath = 0;

    public GlobalDifficultyManager(MinecraftServer server) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        DifficultyDeathScaler.LOGGER.info("Loading global difficulty data");
        final var state = StateSaver.getServerState(server);
        delayFirstTask(state.timeBeforeReduce);
        increaser = new DifficultyIncrease(this, timer, state.timeBeforeIncrease, state.increaseEnabled);
        setNumberOfDeath(state.deaths, true);
        totalOfDeath = state.totalOfDeath;

        // update difficulty after restart
        server.setDifficulty(getUpdater().getDifficulty(), true);

        updateModifiersValue(getModifiers(numberOfDeath));
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        final var difficulty = updater.getDifficulty();
        server.setDifficulty(difficulty, true);

        updateModifiersValue(updater);

        if (updateType == UpdateType.INCREASE) totalOfDeath++;

        final var pm = server.getPlayerManager();

        pm.getPlayerList().forEach(p -> {
            updater.getModifiers().forEach(m -> {
                if (m instanceof final HealthModifier mod) mod.apply(p);
                else if (m instanceof final FallDamageMultiplierModifier mod) mod.apply(p);
            });
            playSoundUpdate(updateType, p);
        });

        if (updateType != UpdateType.SILENT)
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
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, net.minecraft.world.@Nullable Difficulty difficulty) {
        final var sb = new StringBuilder();
        sb.append(generateHeaderUpdate(updateType));
        if (difficulty == Difficulty.EASY) {
            sb.append("World Difficulty: §2Easy§r");
        } else if (difficulty == Difficulty.NORMAL) {
            sb.append("Difficulty: §eNormal§r");
        } else {
            sb.append("Difficulty: §cHard§r");
        }
        if (numberOfDeath >= STEPS[1].level()) {
            sb.append("\n\n");
        }
        if (numberOfDeath >= STEPS[STEPS.length-1].level()) {
            sb.append("§cWell... Good luck... you dont have regen anymore§r");
        } else if (numberOfDeath >= STEPS[21].level()) {
            sb.append("§cNether is gonna be very dangerous...§r");
        } else if (numberOfDeath >= STEPS[9].level()) {
            sb.append("§eThis is so fcking annoying!§r");
        } else if (numberOfDeath >= STEPS[5].level()) {
            sb.append("§eMobs are modified, right?...§r");
        } else if (numberOfDeath >= STEPS[3].level()) {
            sb.append("§2Normal difficulty is back!§r");
        } else if (numberOfDeath >= STEPS[1].level()) {
            sb.append("§2Oh no, the difficulty is becoming harder.§r");
        }
        sb.append("§r\n\n");

        if (updateType == null && increaser.isEnabled()) updateType = UpdateType.AUTOMATIC_INCREASE;
        sb.append(generateFooterUpdate(STEPS, "no one died", updateType));

        return sb.toString();
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        HealthModifier.apply(player, healthModifier);
        FallDamageMultiplierModifier.apply(player, fallDamageMultiplierModifier);
    }

    public void onEntitySpawn(HostileEntity hostile) {
        FollowRangeModifier.apply(hostile, followRangeModifier);
        StepHeightModifier.apply(hostile, stepHeightModifier);
        SpawnReinforcementsModifier.apply(hostile, spawnReinforcementModifier);
        // replace a piglin by two piglins brutes (for 50 piglins)
        if (SPAWN_PIGLIN_BRUTE && !hostile.hasCustomName() && hostile instanceof PiglinEntity && Math.random()*100 < 2) {
            EntityType.PIGLIN_BRUTE.spawn((ServerWorld) hostile.getWorld(), hostile.getBlockPos(), SpawnReason.MOB_SUMMONED);
            EntityType.PIGLIN_BRUTE.spawn((ServerWorld) hostile.getWorld(), hostile.getBlockPos(), SpawnReason.MOB_SUMMONED);
            hostile.discard();
        }
    }

    @Override
    public void save() {
        DifficultyDeathScaler.LOGGER.info("Saving global difficulty data");
        final var state = StateSaver.getServerState(server);
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = delay();
        state.timeBeforeIncrease = increaser.delay();
        state.increaseEnabled = increaser.isEnabled();
        state.totalOfDeath = totalOfDeath;
    }

    public int getTotalOfDeath() {
        return totalOfDeath;
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

    public static boolean canSpawnPiglinsBrute() {
        return SPAWN_PIGLIN_BRUTE;
    }

    public double getHealthModifier() {
        return healthModifier;
    }
}
