package world.anhgelus.architectsland.difficultydeathscaler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private int numberOfDeath = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }
            increaseDeath(player);
            return true;
        });
    }

    private void increaseDeath(ServerPlayerEntity player) {
        numberOfDeath++;
        final var server = player.getServerWorld().getServer();
        final var timer = new Timer();
        final var reducer = new TimerTask() {
            private int lastNumberOfDeath = numberOfDeath;
            @Override
            public void run() {
                if (numberOfDeath != lastNumberOfDeath) {
                    timer.cancel();
                    return;
                }
                decreaseDeath(server);
                lastNumberOfDeath = numberOfDeath;
                if (numberOfDeath == 0) timer.cancel();
            }
        };
        timer.schedule(reducer,24*60*60*1000L, 24*60*60*1000L);
        updateDeath(server, true);
    }

    private void decreaseDeath(MinecraftServer server) {
        if (numberOfDeath >= 7) {
            numberOfDeath = 5;
        } else if (numberOfDeath >= 5) {
            numberOfDeath = 3;
        } else if (numberOfDeath >= 3) {
            numberOfDeath = 1;
        } else if (numberOfDeath >= 1) {
            numberOfDeath = 0;
        }
        updateDeath(server, false);
    }

    private void updateDeath(@NotNull MinecraftServer server, boolean playSound) {
        final var gamerules = server.getGameRules();
        final var sleeping = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        final var naturelRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION);

        Difficulty difficulty = Difficulty.NORMAL;
        if (numberOfDeath >= 7) {
            naturelRegeneration.set(false, server);
            sleeping.set(100, server);
            difficulty = Difficulty.HARD;
        } else if (numberOfDeath >= 5) {
            naturelRegeneration.set(true, server);
            sleeping.set(100, server);
            difficulty = Difficulty.HARD;
        } else if (numberOfDeath >= 3) {
            naturelRegeneration.set(true, server);
            sleeping.set(70, server);
            difficulty = Difficulty.HARD;
        } else if (numberOfDeath >= 1) {
            naturelRegeneration.set(true, server);
            sleeping.set(70, server);
        } else if (numberOfDeath == 0) {
            naturelRegeneration.set(true, server);
            sleeping.set(30, server);
        }
        if (List.of(0,1,3,5,7).contains(numberOfDeath)) {
            server.setDifficulty(difficulty, true);
            server.getPlayerManager().broadcast(Text.of(generateDifficultyUpdate(server, difficulty)), false);
            if (playSound) {
                server.getPlayerManager().getPlayerList().forEach(p -> {
                    p.playSoundToPlayer(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 1, 1.2f);
                });
            }
        }
    }

    private @NotNull String generateDifficultyUpdate(@NotNull MinecraftServer server, Difficulty difficulty) {
        final var gamerules = server.getGameRules();
        final var percentage = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).get();
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION).get();
        final var sb = new StringBuilder();
        sb.append("§8=============== §rDifficulty update! §8===============§r\n");
        if (difficulty == Difficulty.NORMAL) {
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
            sb.append("§2yes");
        } else {
            sb.append("§cno");
        }
        sb.append("§r\n");
        sb.append("§8=============================================§r");
        return sb.toString();
    }
}
