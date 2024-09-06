package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.BlockBreakSpeedModifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.LuckModifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.PlayerHealthModifier;

import java.util.*;

public class PlayerDifficultyManager extends DifficultyManager {
    public @Nullable ServerPlayerEntity player;
    public @Nullable UUID uuid = null;

    public static final int SECONDS_BEFORE_DECREASED = 12*60*60;
    public static final int MAX_DEATHS_DAY = 5;

    public static class HealthModifier extends PlayerHealthModifier {
        public static final Identifier ID = Identifier.of(PREFIX + "player_health_modifier");

        public HealthModifier() {
            super(ID);
        }

        public static void apply(ServerPlayerEntity player, double value) {
            apply(ID, ATTRIBUTE, OPERATION, player, value);
        }
    }

    public static final StepPair[] STEPS = new StepPair[]{
            new StepPair(0, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(0);
                updater.getModifier(LuckModifier.class).update(0.1);
                updater.getModifier(BlockBreakSpeedModifier.class).update(0.1);
            }),
            new StepPair(2, (server, gamerules, updater) -> {
                updater.getModifier(LuckModifier.class).update(0);
            }),
            new StepPair(3, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-2);
            }),
            new StepPair(4, (server, gamerules, updater) -> {
                updater.getModifier(BlockBreakSpeedModifier.class).update(0);
            }),
            new StepPair(5, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-4);
            }),
            new StepPair(7, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-6);
            }),
            new StepPair(8, (server, gamerules, updater) -> {
                updater.getModifier(LuckModifier.class).update(-0.1);
            }),
            new StepPair(10, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-8);
            }),
            new StepPair(12, (server, gamerules, updater) -> {
                updater.getModifier(BlockBreakSpeedModifier.class).update(-0.1);
            }),
            new StepPair(13, (server, gamerules, updater) -> {
                updater.getModifier(LuckModifier.class).update(-0.2);
            }),
            new StepPair(15, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-10);
            }),
    };

    protected double healthModifier = 0;
    protected double luckModifier = 0;
    protected double blockBreakSpeedModifier = 0;

    private int deathDay;
    private final List<Long> deathDayStart = new ArrayList<>();

    private int totalOfDeath = 0;

    public PlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);
        this.player = player;

        DifficultyDeathScaler.LOGGER.info("Loading player {} difficulty data", player.getUuid());
        loadData(StateSaver.getPlayerState(player));
    }

    public PlayerDifficultyManager(MinecraftServer server, @NotNull UUID uuid, PlayerData data) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        this.uuid = uuid;

        DifficultyDeathScaler.LOGGER.info("Creating player difficulty manager with data");
        loadData(data);
    }

    private void loadData(PlayerData data) {
        numberOfDeath = data.deaths;
        deathDay = data.deathDay;
        totalOfDeath = data.totalOfDeath;
        for (final var delay : data.deathDayDelay) {
            deathDayStart.add(delay);
        }
        for (final var delay : data.deathDayDelay) {
            try {
                timer.schedule(deathDayTask(), (24*60*60 - delay)*1000L);
            } catch (IllegalArgumentException e) {
                DifficultyDeathScaler.LOGGER.error("An error occurred while loading data", e);
                DifficultyDeathScaler.LOGGER.warn("Removing one day death");
                deathDay--;
            }
        }
        delayFirstTask(data.timeBeforeReduce);
        updateTimerTask();
        updateModifiersValue(modifiers(numberOfDeath));
    }


    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        updateModifiersValue(updater);

        if (player == null) return;
        player.sendMessage(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);

        playSoundUpdate(updateType, player);
    }

    @Override
    protected void onDeath(UpdateType updateType, Updater updater) {
        if (updateType == UpdateType.SET) return;
        deathDay++;
        deathDayStart.add(delay(System.currentTimeMillis() / 1000));

        if (player == null) throw new IllegalStateException("Player is null");
        if (player.getWorld().isClient()) return;
        timer.schedule(deathDayTask(), 24*60*60*1000L);
        kickIfDiedTooMuch();
    }

    @Override
    protected void updateModifiersValue(List<Modifier<?>> modifiers) {
        modifiers.forEach(m -> {
            if (m instanceof final HealthModifier mod) {
                healthModifier = mod.getValue();
                mod.apply(player);
            } else if (m instanceof final LuckModifier mod) {
                luckModifier = mod.getValue();
                mod.apply(player);
            } else if (m instanceof final BlockBreakSpeedModifier mod) {
                blockBreakSpeedModifier = mod.getValue();
                mod.apply(player);
            }
        });
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
        final var heartAmount = (20 + healthModifier) / 2;

        final var sb = new StringBuilder();
        sb.append(generateHeaderUpdate(updateType));

        if (deathDay != 0) {
            sb.append("You died ");
            if (deathDay >= 4) {
                sb.append("§c");
            } else if (deathDay >= 2) {
                sb.append("§e");
            } else {
                sb.append("§2");
            }
            sb.append(deathDay).append("§r time");
            if (deathDay > 0) {
                sb.append("s");
            }
            sb.append(" in less than 24 hours.\n");
        }
        sb.append("\n");

        sb.append("Max hearts: ");
        if (heartAmount == 10) {
            sb.append("§2");
        } else if (heartAmount >= 8) {
            sb.append("§e");
        } else {
            sb.append("§c");
        }
        sb.append(heartAmount).append(" ❤§r\n\n");

        sb.append(generateFooterUpdate(STEPS, updateType));

        return sb.toString();
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        applyModifiers();
    }

    @Override
    public void save() {
        assert player != null || uuid != null;
        PlayerData state;
        if (player == null) {
            DifficultyDeathScaler.LOGGER.info("Saving player with uuid {} difficulty data", uuid);
            state = StateSaver.getPlayerState(server, uuid);
        } else {
            DifficultyDeathScaler.LOGGER.info("Saving player ({}) difficulty data", player.getUuid());
            state = StateSaver.getPlayerState(player);
        }
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = delay();
        state.deathDay = deathDay;
        state.totalOfDeath = totalOfDeath;
        var starts = new long[deathDayStart.size()];
        for (int i = 0; i < deathDayStart.size(); i++) {
            starts[i] = deathDayStart.get(i);
        }
        state.deathDayDelay = starts;
    }

    public void applyModifiers() {
        HealthModifier.apply(player, healthModifier);
        LuckModifier.apply(player, luckModifier);
        BlockBreakSpeedModifier.apply(player, blockBreakSpeedModifier);
    }

    private TimerTask deathDayTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (deathDay != 0) {
                    deathDay--;
                    deathDayStart.removeFirst();
                } else DifficultyDeathScaler.LOGGER.warn("Death day is already equal to 0");
            }
        };
    }

    /**
     * Kick the player if he died too much
     * @return true if the player was kicked
     * @throws IllegalStateException if player is null
     */
    public boolean kickIfDiedTooMuch() {
        if (player == null) throw new IllegalStateException("Player is null");
        return kickIfDiedTooMuch(player.networkHandler);
    }

    /**
     * Kick the player if he died too much
     * @return true if the player was kicked
     */
    public boolean kickIfDiedTooMuch(ServerPlayNetworkHandler handler) {
        if (deathDay >= MAX_DEATHS_DAY) {
            DifficultyDeathScaler.LOGGER.info("Kick");
            handler.disconnect(Text.of("You died too much during 24h..."));
            return true;
        }
        return false;
    }
}
