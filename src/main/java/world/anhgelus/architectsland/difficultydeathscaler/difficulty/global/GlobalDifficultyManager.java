package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;

public class GlobalDifficultyManager extends DifficultyManager {
    public static final int SECONDS_BEFORE_DECREASED = 12*60*60; // 12 hours

    public static final Step[] STEPS = new Step[]{
            new GlobalSteps.Default(),
            new GlobalSteps.First(),
            new GlobalSteps.Second(),
            new GlobalSteps.Third(),
            new GlobalSteps.Fourth(),
            new GlobalSteps.Fifth(),
            new GlobalSteps.Sixth(),
            new GlobalSteps.Seventh(),
            new GlobalSteps.Eighth(),
            new GlobalSteps.Ninth(),
    };

    protected int playerHealthModifierValue = 0;

    public GlobalDifficultyManager(MinecraftServer server) {
        super(server);
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        final var pm = server.getPlayerManager();
        if (updateType != UpdateType.SILENT) {
            pm.broadcast(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);
        }

        pm.getPlayerList().forEach(p -> {
            updater.getModifiers().forEach(m -> {
                if (m instanceof final PlayerHealthModifier phm) playerHealthModifierValue = phm.getValue();
                m.apply(p);
            });
            playSoundUpdate(updateType, p);
        });
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, net.minecraft.world.@Nullable Difficulty difficulty) {
        final var gamerules = server.getGameRules();
        final var percentage = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).get();
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION).get();
        final var heartAmount = (20 + playerHealthModifierValue) / 2;

        final var sb = new StringBuilder();
        if (updateType == UpdateType.INCREASE) {
            sb.append("§8============== §rDifficulty increase! §8==============§r\n");
        } else if (updateType == UpdateType.DECREASE) {
            sb.append("§8============== §rDifficulty decrease! §8==============§r\n");
        } else if (updateType == UpdateType.SET) {
            sb.append("§8=============== §rDifficulty change! §8===============§r\n");
        } else {
            sb.append("§8============== §rCurrent difficulty : §8==============§r\n");
        }
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

        sb.append("Player max heart: ");
        if (heartAmount == 10) {
            sb.append("§2");
        } else if (heartAmount >= 9) {
            sb.append("§e");
        } else {
            sb.append("§c");
        }
        sb.append(heartAmount).append(" ❤§r\n");

        sb.append("Natural regeneration: ");
        if (naturalRegeneration) {
            sb.append("§2On");
        } else {
            sb.append("§cOff");
        }
        sb.append("§r\n\n");

        if (numberOfDeath >= STEPS[1].level) {
            if (updateType == UpdateType.DECREASE) {
                sb.append("You only need to survive for §6")
                        .append(printTime(SECONDS_BEFORE_DECREASED))
                        .append("§r to make the difficulty decrease again.");
            } else if (updateType != UpdateType.INCREASE) {
                sb.append("You only need to survive for §6")
                        .append(printTime(SECONDS_BEFORE_DECREASED - System.currentTimeMillis() / 1000 + timerStart))
                        .append("§r to make the difficulty decrease.");
            } else if (numberOfDeath < STEPS[2].level) {
                sb.append("You were on the lowest difficulty for §6")
                        .append(printTime(System.currentTimeMillis() / 1000 - timerStart))
                        .append("§r, but you had to die and ruin everything, didn't you ?");
            } else {
                sb.append("If no one died for §6")
                        .append(printTime(SECONDS_BEFORE_DECREASED - System.currentTimeMillis() / 1000 + timerStart))
                        .append("§r, then the difficulty would’ve decreased... But you chose your fate.");
            }
        } else {
            sb.append("The difficulty cannot get lower. Congratulations!");
        }

        sb.append("\n§8=============================================§r");
        return sb.toString();
    }

    @Override
    protected int getSecondsBeforeDecreased() {
        return SECONDS_BEFORE_DECREASED;
    }

    @Override
    protected Step[] getSteps() {
        return STEPS;
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        PlayerHealthModifier.apply(player, playerHealthModifierValue);
    }
}
