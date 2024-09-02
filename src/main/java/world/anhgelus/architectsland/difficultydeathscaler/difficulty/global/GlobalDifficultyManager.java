package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;

public class GlobalDifficultyManager extends DifficultyManager {
    public static final int SECONDS_BEFORE_DECREASED = 12*60*60; // 12 hours

    public static final StepPair[] STEPS = new StepPair[]{
            new StepPair(0, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(30, server);
                gamerules.get(GameRules.NATURAL_REGENERATION).set(true, server);
                updater.updateDifficulty(2);
            }),
            new StepPair(5, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(70, server);
            }),
            new StepPair(10, (server, gamerules, updater) -> updater.updateDifficulty(3)),
            new StepPair(15, (server, gamerules, updater) -> {
                gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(100, server);
            }),
            new StepPair(20, (server, gamerules, updater) -> {
                gamerules.get(GameRules.NATURAL_REGENERATION).set(false, server);
            }),
    };

    public GlobalDifficultyManager(MinecraftServer server) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        DifficultyDeathScaler.LOGGER.info("Loading global difficulty data");
        final var state = StateSaver.getServerState(server);
        numberOfDeath = state.deaths;
        delayFirstTask(state.timeBeforeReduce);
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        final var pm = server.getPlayerManager();

        pm.getPlayerList().forEach(p -> playSoundUpdate(updateType, p));

        if (updateType != UpdateType.SILENT) {
            pm.broadcast(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);
        }
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, net.minecraft.world.@Nullable Difficulty difficulty) {
        final var gamerules = server.getGameRules();
        final var percentage = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).get();
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION).get();

        final var sb = new StringBuilder();
        sb.append(generateHeaderUpdate(updateType));
        if (difficulty == net.minecraft.world.Difficulty.NORMAL) {
            sb.append("Difficulty: §2Normal§r");
        } else {
            sb.append("Difficulty: §cHard§r");
        }
        sb.append("\n");
        sb.append("Players sleeping percentage to skip the night: ");
        if (percentage == 30) {
            sb.append("§2");
        } else if (percentage == 70) {
            sb.append("§e");
        } else {
            sb.append("§c");
        }
        sb.append(percentage).append("%§r\n");

        sb.append("Natural regeneration: ");
        if (naturalRegeneration) {
            sb.append("§2On");
        } else {
            sb.append("§cOff");
        }
        sb.append("§r\n\n");

        sb.append(generateFooterUpdate(STEPS, updateType));

        return sb.toString();
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        //
    }

    @Override
    public void save() {
        DifficultyDeathScaler.LOGGER.info("Saving global difficulty data");
        final var state = StateSaver.getServerState(server);
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = System.currentTimeMillis() / 1000 - timerStart;
    }
}
