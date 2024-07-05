package world.anhgelus.architectsland.difficultydeathscaler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private int numberOfDeath = 0;
    // Each death count when difficulty steps up
    private final int[] deathSteps = {0, 1, 3, 5, 7, 10, 12, 15, 17, 20};

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("death_difficulty_health_modifier");
    private double playerHealthModifierValue = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        // set up difficulty of deathSteps[0]
        ServerLifecycleEvents.SERVER_STARTED.register(server -> updateDeath(server, false));

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }
            increaseDeath(player);
            return true;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyHealthModifierToPlayer(handler.player));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> applyHealthModifierToPlayer(newPlayer));
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
        for (int i = deathSteps.length - 1; i >= 0; i--) {
            // needed to prevent accessing deathSteps[-1]
            if (i == 0) {
                numberOfDeath = 0;
            } else if (numberOfDeath >= deathSteps[i]) {
                numberOfDeath = deathSteps[i-1];
                break;
            }
        }
        updateDeath(server, false);
    }

    private void updateDeath(@NotNull MinecraftServer server, boolean difficultyIncrease) {
        final var gamerules = server.getGameRules();
        final var sleeping = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION);

        Difficulty difficulty = Difficulty.NORMAL;
        naturalRegeneration.set(true, server);
        sleeping.set(30, server);
        playerHealthModifierValue = 0;
        if (numberOfDeath >= deathSteps[1]) {
            sleeping.set(70, server);
        }
        if (numberOfDeath >= deathSteps[2]) {
            difficulty = Difficulty.HARD;
        }
        if (numberOfDeath >= deathSteps[3]) {
            sleeping.set(100, server);
        }
        if (numberOfDeath >= deathSteps[4]) {
            playerHealthModifierValue = -2;
        }
        if (numberOfDeath >= deathSteps[5]) {
            playerHealthModifierValue = -4;
        }
        if (numberOfDeath >= deathSteps[6]) {
            playerHealthModifierValue = -6;
        }
        if (numberOfDeath >= deathSteps[7]) {
            playerHealthModifierValue = -8;
        }
        if (numberOfDeath >= deathSteps[8]) {
            playerHealthModifierValue = -10;
        }
        if (numberOfDeath >= deathSteps[9]) {
            // on va tous crever à ce point lol
            naturalRegeneration.set(false, server);
        }

        if (Arrays.stream(deathSteps).anyMatch(x -> x == numberOfDeath)) {
            server.setDifficulty(difficulty, true);
            server.getPlayerManager().broadcast(Text.of(generateDifficultyUpdate(server, difficulty)), false);
            server.getPlayerManager().getPlayerList().forEach(p -> {
                applyHealthModifierToPlayer(p);

                if (difficultyIncrease) {
                    p.playSoundToPlayer(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                            SoundCategory.AMBIENT,
                            1,
                            1.2f
                    );
                } else {
                    p.playSoundToPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                            SoundCategory.AMBIENT,
                            1,
                            1
                    );
                }
            });
        }
    }

    private void applyHealthModifierToPlayer(ServerPlayerEntity player) {
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

    private @NotNull String generateDifficultyUpdate(@NotNull MinecraftServer server, Difficulty difficulty) {
        final var gamerules = server.getGameRules();
        final var percentage = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).get();
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION).get();
        final var heartAmount = (20 + playerHealthModifierValue) / 2;

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
        sb.append("§r\n");
        sb.append("§8=============================================§r");
        return sb.toString();
    }
}
