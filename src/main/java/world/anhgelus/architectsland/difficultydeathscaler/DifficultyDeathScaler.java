package world.anhgelus.architectsland.difficultydeathscaler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
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

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DifficultyDeathScaler implements ModInitializer {
    public static final String MOD_ID = "difficulty-death-scaler";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    // Each death count when difficulty steps up
    public static final int[] DEATH_STEPS = {0, 1, 3, 5, 7, 10, 12, 15, 17, 20};
    // timer
    public static final int SECONDS_BEFORE_DECREASE = 12*60*60;

    private int numberOfDeath = 0;

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("death_difficulty_health_modifier");
    private double playerHealthModifierValue = 0;

    private long timerStart = (new Date()).getTime() / 1000;

    @Override
    public void onInitialize() {
        LOGGER.info("Difficulty Death Scaler started");

        final LiteralArgumentBuilder<ServerCommandSource> command = literal("difficultydeathscaler");
        command.then(literal("get").executes(context -> {
            final var source = context.getSource();
            final var server = source.getServer();
            source.sendFeedback(() -> Text.literal(generateDifficultyUpdate(server, server.getOverworld().getDifficulty(), DifficultyUpdateType.GET)), false);
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("set")
                .requires(source -> source.hasPermissionLevel(1))
                .then(argument("number of death", IntegerArgumentType.integer())
                .executes(context -> {
                    final var source = context.getSource();
                    final var server = source.getServer();
                    numberOfDeath = IntegerArgumentType.getInteger(context, "number of death");
                    updateDeath(server, DifficultyUpdateType.INCREASE);
                    setupTimer(server);
                    source.sendFeedback(() -> Text.literal("The difficulty has been changed"), true);
                    return Command.SINGLE_SUCCESS;
                })
                )
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));


        // set up difficulty of deathSteps[0]
        ServerLifecycleEvents.SERVER_STARTED.register(server -> updateDeath(server, DifficultyUpdateType.INCREASE));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                increaseDeath(player);
                return;
            }
            if (entity instanceof WitherEntity ||
                    entity instanceof EnderDragonEntity ||
                    entity instanceof ElderGuardianEntity ||
                    entity instanceof WardenEntity) {
                decreaseDeath(entity.getServer());
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyHealthModifierToPlayer(handler.player));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> applyHealthModifierToPlayer(newPlayer));
    }

    private void increaseDeath(ServerPlayerEntity player) {
        numberOfDeath++;
        final var server = player.getServerWorld().getServer();
        updateDeath(server, DifficultyUpdateType.INCREASE);
        setupTimer(server);
    }

    private void setupTimer(MinecraftServer server) {
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
        timer.schedule(reducer,SECONDS_BEFORE_DECREASE*1000L, SECONDS_BEFORE_DECREASE*1000L);
        timerStart = (new Date()).getTime() / 1000;
    }

    private void decreaseDeath(MinecraftServer server) {
        for (int i = DEATH_STEPS.length - 1; i >= 0; i--) {
            // needed to prevent accessing deathSteps[-1]
            if (i == 0) {
                numberOfDeath = 0;
            } else if (numberOfDeath >= DEATH_STEPS[i]) {
                numberOfDeath = DEATH_STEPS[i-1];
                break;
            }
        }
        updateDeath(server, DifficultyUpdateType.DECREASE);
    }

    private void updateDeath(@NotNull MinecraftServer server, DifficultyUpdateType updateType) {
        if (updateType == DifficultyUpdateType.GET) {
            // Shouldn’t happen
            return;
        }

        final var gamerules = server.getGameRules();
        final var sleeping = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION);

        Difficulty difficulty = Difficulty.NORMAL;
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

        if (Arrays.stream(DEATH_STEPS).anyMatch(x -> x == numberOfDeath)) {
            server.setDifficulty(difficulty, true);
            server.getPlayerManager().broadcast(Text.of(generateDifficultyUpdate(server, difficulty, updateType)), false);
            server.getPlayerManager().getPlayerList().forEach(p -> {
                applyHealthModifierToPlayer(p);

                if (updateType == DifficultyUpdateType.INCREASE) {
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

    private enum DifficultyUpdateType {
        INCREASE,
        DECREASE,
        GET
    }

    private @NotNull String generateDifficultyUpdate(@NotNull MinecraftServer server, Difficulty difficulty, DifficultyUpdateType updateType) {
        final var gamerules = server.getGameRules();
        final var percentage = gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).get();
        final var naturalRegeneration = gamerules.get(GameRules.NATURAL_REGENERATION).get();
        final var heartAmount = (20 + playerHealthModifierValue) / 2;

        final var sb = new StringBuilder();
        if (updateType == DifficultyUpdateType.INCREASE) {
            sb.append("§8============== §rDifficulty increase! §8==============§r\n");
        } else if (updateType == DifficultyUpdateType.DECREASE) {
            sb.append("§8============== §rDifficulty decrease! §8==============§r\n");
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

        if (updateType == DifficultyUpdateType.GET) {
            sb.append("You only need to survive for §6")
                    .append(printTime(SECONDS_BEFORE_DECREASE - new Date().getTime() / 1000 + timerStart))
                    .append("§r to make the difficulty decrease.");
        } else {
            sb.append("If no one died for §6")
                    .append(printTime(SECONDS_BEFORE_DECREASE - new Date().getTime() / 1000 + timerStart))
                    .append("§r, then the difficulty would’ve decreased... But you chose your fate.");
        }
        sb.append("\n§8=============================================§r");
        return sb.toString();
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