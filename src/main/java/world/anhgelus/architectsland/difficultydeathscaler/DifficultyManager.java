package world.anhgelus.architectsland.difficultydeathscaler;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DifficultyManager {
    // Each death count when difficulty steps up
    public static final int[] DEATH_STEPS = {0, 1, 3, 5, 7, 10, 12, 15, 17, 20};

    // timer
    public final int secondsBeforeDecrease;
    private int numberOfDeath;

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("death_difficulty_health_modifier");
    private double playerHealthModifierValue = 0;

    private long timerStart = System.currentTimeMillis() / 1000;

    private TimerTask reducerTask;
    private final Timer difficultyTimer = new Timer();

    private enum UpdateType {
        INCREASE,
        DECREASE,
        GET,
        SET,
        SILENT
    }

    public DifficultyManager() {
        numberOfDeath = 0;
        secondsBeforeDecrease = 12*60*60;
    }

    public DifficultyManager(int numberOfDeath, int secondsBeforeDecrease, MinecraftServer server) {
        this.numberOfDeath = numberOfDeath;
        this.secondsBeforeDecrease = secondsBeforeDecrease;
        this.updateTimerTask(server);
    }

    public void setNumberOfDeath(MinecraftServer server, int n, boolean silent) {
        numberOfDeath = n;
        if (silent) {
            updateDeath(server, UpdateType.SILENT);
        } else {
            updateDeath(server, UpdateType.SET);
        }
        updateTimerTask(server);
    }

    public int getNumberOfDeath() {
        return numberOfDeath;
    }

    public void increaseDeath(MinecraftServer server) {
        numberOfDeath++;
        updateDeath(server, UpdateType.INCREASE);
        updateTimerTask(server);
    }

    public void updateTimerTask(MinecraftServer server) {
        if (reducerTask != null) reducerTask.cancel();
        if (numberOfDeath == 0) return;
        reducerTask = new TimerTask() {
            @Override
            public void run() {
                decreaseDeath(server, true);
                if (numberOfDeath == 0) reducerTask.cancel();
            }
        };
        difficultyTimer.schedule(reducerTask,secondsBeforeDecrease*1000L, secondsBeforeDecrease*1000L);
        timerStart = System.currentTimeMillis() / 1000;
    }

    public void decreaseDeath(MinecraftServer server) {
        // Avoids updating the difficulty when it can’t go lower.
        // Prevents for example the difficulty decrease message when killing a boss if the difficulty doesn't decrease.
        if (numberOfDeath < DEATH_STEPS[1]) {
            numberOfDeath = 0;
            return;
        }

        for (int i = DEATH_STEPS.length - 1; i > 0; i--) {
            if (numberOfDeath >= DEATH_STEPS[i]) {
                numberOfDeath = DEATH_STEPS[i-1];
                break;
            }
        }

        updateDeath(server, UpdateType.DECREASE);
    }

    public void decreaseDeath(MinecraftServer server, boolean updateTimer) {
        if (updateTimer) {
            timerStart = System.currentTimeMillis() / 1000;
        }

        decreaseDeath(server);
    }

    private void updateDeath(@NotNull MinecraftServer server, UpdateType updateType) {
        if (updateType == UpdateType.GET) {
            throw new IllegalArgumentException("Cannot update difficulty when only getting difficulty");
        }

        final var gamerules = server.getGameRules();
        final var sleeping = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION);

        var difficulty = Difficulty.NORMAL;
        naturalRegeneration.set(true, server);
        sleeping.set(30, server);
        playerHealthModifierValue = 0;
        if (numberOfDeath >= DEATH_STEPS[1]) {
            sleeping.set(70, server);
        }
        if (numberOfDeath >= DEATH_STEPS[2]) {
            difficulty = Difficulty.HARD;
        }
        if (numberOfDeath >= DEATH_STEPS[3]) {
            sleeping.set(100, server);
        }
        if (numberOfDeath >= DEATH_STEPS[4]) {
            playerHealthModifierValue = -2;
        }
        if (numberOfDeath >= DEATH_STEPS[5]) {
            playerHealthModifierValue = -4;
        }
        if (numberOfDeath >= DEATH_STEPS[6]) {
            playerHealthModifierValue = -6;
        }
        if (numberOfDeath >= DEATH_STEPS[7]) {
            playerHealthModifierValue = -8;
        }
        if (numberOfDeath >= DEATH_STEPS[8]) {
            playerHealthModifierValue = -10;
        }
        if (numberOfDeath >= DEATH_STEPS[9]) {
            // on va tous crever à ce point lol
            naturalRegeneration.set(false, server);
        }

        if (Arrays.stream(DEATH_STEPS).anyMatch(x -> x == numberOfDeath) || updateType == UpdateType.SET) {
            server.setDifficulty(difficulty, true);

            if (updateType != UpdateType.SILENT) {
                server.getPlayerManager().broadcast(Text.of(generateDifficultyUpdate(server, difficulty, updateType)), false);
            }

            server.getPlayerManager().getPlayerList().forEach(p -> {
                applyHealthModifierToPlayer(p);

                if (updateType == UpdateType.INCREASE || updateType == UpdateType.SET) {
                    p.playSoundToPlayer(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                            SoundCategory.AMBIENT,
                            1,
                            1.2f
                    );
                } else if (updateType == UpdateType.DECREASE) {
                    p.playSoundToPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                            SoundCategory.AMBIENT,
                            1,
                            1
                    );
                }
            });
        }
    }

    public void applyHealthModifierToPlayer(ServerPlayerEntity player) {
        EntityAttributeInstance healthAttributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttributeInstance != null) {
            healthAttributeInstance.removeModifier(HEALTH_MODIFIER_ID);
            if (playerHealthModifierValue == 0) { return; }

            EntityAttributeModifier playerHealthModifier = new EntityAttributeModifier(
                    HEALTH_MODIFIER_ID,
                    playerHealthModifierValue,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );
            healthAttributeInstance.addPersistentModifier(playerHealthModifier);
        }
    }

    private @NotNull String generateDifficultyUpdate(@NotNull MinecraftServer server, Difficulty difficulty, UpdateType updateType) {
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

        if (updateType == UpdateType.GET || updateType == UpdateType.DECREASE) {
            sb.append("You only need to survive for §6")
                    .append(printTime(secondsBeforeDecrease - System.currentTimeMillis() / 1000 + timerStart))
                    .append(" §rto make the difficulty decrease.");
        } else {
            sb.append("If no one died for §6")
                    .append(printTime(secondsBeforeDecrease - System.currentTimeMillis() / 1000 + timerStart))
                    .append("§r, then the difficulty would’ve decreased... But you chose your fate.");
        }
        sb.append("\n§8=============================================§r");
        return sb.toString();
    }

    public String getDifficultyUpdate(@NotNull MinecraftServer server, Difficulty difficulty) {
        return generateDifficultyUpdate(server, difficulty, UpdateType.GET);
    }

    private static String printTime(long time) {
        long hours = 0;
        if (time > 3600) {
            hours = Math.floorDiv(time, 3600);
        }
        long minutes = 0;
        if (hours != 0 || time > 60) {
            minutes = Math.floorDiv(time - hours * 3600, 60);
        }
        long seconds = (long) Math.floor(time - hours * 3600 - minutes * 60);
        return String.format("%d hours %d minutes %d seconds", hours, minutes, seconds);
    }
}
