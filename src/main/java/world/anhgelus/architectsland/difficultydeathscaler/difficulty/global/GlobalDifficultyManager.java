package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static final StepPair[] STEPS = new StepPair[]{
            new StepPair(0, (server, gamerules, updater) -> {
                // mobs
                gamerules.get(GameRules.DO_INSOMNIA).set(false, server);
                gamerules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(true, server);
                gamerules.get(GameRules.UNIVERSAL_ANGER).set(false, server);
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
            new StepPair(3, (server, gamerules, updater) -> {
                gamerules.get(GameRules.BLOCK_EXPLOSION_DROP_DECAY).set(true, server);
            }),
            new StepPair(5, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(70, server);
            }),
            new StepPair(7, (server, gamerules, updater) -> {
                gamerules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(true, server);
            }),
            new StepPair(10, (server, gamerules, updater) -> updater.updateDifficulty(2)),
            new StepPair(12, (server, gamerules, updater) -> {
                updater.getModifier(FollowRangeModifier.class).update(0.25);
            }),
            new StepPair(13, (server, gamerules, updater) -> {
                gamerules.get(GameRules.DO_INSOMNIA).set(true, server);
            }),
            new StepPair(15, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-2);
            }),
            new StepPair(16, (server, gamerules, updater) -> {
                updater.getModifier(StepHeightModifier.class).update(0.5);
            }),
            new StepPair(18, (server, gamerules, updater) -> {
                updater.getModifier(FallDamageMultiplierModifier.class).update(0.25);
            }),
            new StepPair(20, (server, gamerules, updater) -> {
                gamerules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(true, server);
            }),
            new StepPair(21, (server, gamerules, updater) -> {
                updater.getModifier(SpawnReinforcementsModifier.class).update(0.25);
            }),
            new StepPair(22, (server, gamerules, updater) -> {
                gamerules.get(GameRules.REDUCED_DEBUG_INFO).set(true, server);
            }),
            new StepPair(23, (server, gamerules, updater) -> {
                updater.getModifier(FallDamageMultiplierModifier.class).update(0.5);
            }),
            new StepPair(24, (server, gamerules, updater) -> {
                gamerules.get(GameRules.WATER_SOURCE_CONVERSION).set(false, server);
            }),
            new StepPair(25, (server, gamerules, updater) -> updater.updateDifficulty(3)),
            new StepPair(26, (server, gamerules, updater) -> {
                updater.getModifier(StepHeightModifier.class).update(1);
            }),
            new StepPair(27, (server, gamerules, updater) -> {
                updater.getModifier(FollowRangeModifier.class).update(0.35);
            }),
            new StepPair(28, (server, gamerules, updater) -> {
                gamerules.get(GameRules.DO_LIMITED_CRAFTING).set(true, server);
            }),
            new StepPair(30, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-4);
            }),
            new StepPair(32, (server, gamerules, updater) -> {
                gamerules.get(GameRules.UNIVERSAL_ANGER).set(true, server);
            }),
            new StepPair(33, (server, gamerules, updater) -> {
                updater.getModifier(SpawnReinforcementsModifier.class).update(0.5);
            }),
            new StepPair(35, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(100, server);
            }),
            new StepPair(37, (server, gamerules, updater) -> {
                updater.getModifier(FallDamageMultiplierModifier.class).update(1);
            }),
            new StepPair(38, (server, gamerules, updater) -> {
                gamerules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(false, server);
            }),
            new StepPair(40, (server, gamerules, updater) -> {
                gamerules.get(GameRules.NATURAL_REGENERATION).set(false, server);
            }),
    };

    protected double healthModifier = 0;
    protected double followRangeModifier = 0;
    protected double stepHeightModifier = 0;
    protected double spawnReinforcementModifier = 0;
    protected double fallDamageMultiplierModifier = 0;

    public GlobalDifficultyManager(MinecraftServer server) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        DifficultyDeathScaler.LOGGER.info("Loading global difficulty data");
        final var state = StateSaver.getServerState(server);
        delayFirstTask(state.timeBeforeReduce);
        increaser = new DifficultyIncrease(this, timer, state.timeBeforeIncrease, state.increaseEnabled);
        setNumberOfDeath(state.deaths, true);

        updateModifiersValue(modifiers(numberOfDeath));
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        updateModifiersValue(updater);

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
        if (numberOfDeath >= STEPS[14].level()) {
            sb.append("§cWell... Good luck... you dont have regen anymore§r");
        } else if (numberOfDeath >= STEPS[11].level()) {
            sb.append("§cNether is gonna be very dangerous...§r");
        } else if (numberOfDeath >= STEPS[7].level()) {
            sb.append("§eOh fck, no more F3§r");
        } else if (numberOfDeath >= STEPS[5].level()) {
            sb.append("§eNormal difficulty is back!§r");
        } else if (numberOfDeath >= STEPS[1].level()) {
            sb.append("§2Oh no, the difficulty is becoming harder.§r");
        }
        sb.append("§r\n\n");

        if (updateType == null && increaser.enabled()) updateType = UpdateType.AUTOMATIC_INCREASE;
        sb.append(generateFooterUpdate(STEPS, updateType));

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
    }

    @Override
    public void save() {
        DifficultyDeathScaler.LOGGER.info("Saving global difficulty data");
        final var state = StateSaver.getServerState(server);
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = delay();
        state.timeBeforeIncrease = increaser.delay();
        state.increaseEnabled = increaser.enabled();
    }
}
