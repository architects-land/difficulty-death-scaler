package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyUpdater;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.BlockBreakSpeedModifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.MovementSpeedModifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.PlayerHealthModifier;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDifficultyManager extends DifficultyManager {
    public static final int SECONDS_BEFORE_DECREASED = 24 * 60 * 60;
    public static final Step[] STEPS = new Step[]{
            new Step(0, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(0);
//                updater.getModifier(LuckModifier.class).update(0.1);
                updater.getModifier(BlockBreakSpeedModifier.class).update(0.4); // is haste 2
                updater.getModifier(MovementSpeedModifier.class).update(0.1); // is speed 1
            }),
            new Step(1, (server, gamerules, updater) -> {
//                updater.getModifier(LuckModifier.class).update(0);
                updater.getModifier(BlockBreakSpeedModifier.class).update(0.2); // is haste 1
                updater.getModifier(MovementSpeedModifier.class).update(0);
            }),
            new Step(2, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-2);
            }),
            new Step(3, (server, gamerules, updater) -> {
                updater.getModifier(BlockBreakSpeedModifier.class).update(0);
                updater.getModifier(HealthModifier.class).update(-4);
            }),
            new Step(5, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-6);
            }),
            new Step(7, (server, gamerules, updater) -> {
                updater.getModifier(MovementSpeedModifier.class).update(-0.1); // is slowness 1
            }),
            new Step(8, (server, gamerules, updater) -> {
                updater.getModifier(BlockBreakSpeedModifier.class).update(-0.2); // is mining fatigue 1
//                updater.getModifier(LuckModifier.class).update(-0.2);
            }),
            new Step(10, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-8);
            }),
            new Step(12, (server, gamerules, updater) -> {
                updater.getModifier(MovementSpeedModifier.class).update(-0.2); // is slowness 2
            }),
            new Step(15, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-10);
            }),
    };
    public static boolean ENABLE_TEMP_BAN = true;
    public static int DEATH_BEFORE_TEMP_BAN = 5;
    public static int TEMP_BAN_DURATION = 12;
    private final GlobalDifficultyManager globalManager;
    private final List<TickTask> deathDayTasks = new ArrayList<>();
    private final List<Long> deathDayEnd = new ArrayList<>();
    public @Nullable ServerPlayerEntity player;
    public @Nullable UUID uuid = null;
    protected double healthModifier = 0;
    //    protected double luckModifier = 0;
    protected double blockBreakSpeedModifier = 0;
    protected double movementSpeedModifier = 0;
    private int deathDay;
    private boolean tempBan;
    private long bannedSince = -1;

    public PlayerDifficultyManager(MinecraftServer server, GlobalDifficultyManager globalManager, ServerPlayerEntity player) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);
        this.player = player;
        this.globalManager = globalManager;

        DifficultyDeathScaler.LOGGER.info("Loading player ({}) difficulty data", player.getUuid());
        load(StateSaver.getPlayerState(player));
    }

    public PlayerDifficultyManager(MinecraftServer server, GlobalDifficultyManager globalManager, @NotNull UUID uuid, PlayerData data) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);

        this.uuid = uuid;
        this.globalManager = globalManager;

        DifficultyDeathScaler.LOGGER.info("Creating player ({}) difficulty manager with data", uuid);
        load(data);
    }

    private void load(PlayerData data) {
        super.load(data);
        bannedSince = data.bannedSince;
        tempBan = bannedSince != -1;
        for (final var timeEnd : data.deathDayEnd) {
            try {
                newDeathDay(timeEnd);
            } catch (IllegalArgumentException e) {
                DifficultyDeathScaler.LOGGER.error("An error occurred while loading data", e);
                DifficultyDeathScaler.LOGGER.warn("Removing one day death");
                deathDay--;
            }
        }
    }

    @Override
    protected void onUpdate(UpdateType updateType, DifficultyUpdater updater) {
        updateModifiersValue(updater);

        if (updateType == UpdateType.SILENT) return;

        if (player == null) {
            DifficultyDeathScaler.LOGGER.warn("Player in {} is null", this);
            return;
        }

        player.sendMessage(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);

        playSoundUpdate(updateType, player);
    }

    @Override
    protected void onDeath(UpdateType updateType, DifficultyUpdater updater) {
        super.onDeath(updateType, updater);

        if (player == null) {
            DifficultyDeathScaler.LOGGER.error("Updating death of null player. UpdateType {}", updateType);
            throw new IllegalStateException("Player is null");
        }
        if (player.getWorld().isClient()) return;
        newDeathDay(24 * 60 * 60 + System.currentTimeMillis() / 1000);
        if (!diedTooMuch()) return;
        // temp ban
        DifficultyDeathScaler.LOGGER.info("{} has skill issue: banned for 12 hours", player.getName().getString());
        tempBan = true;
        bannedSince = System.currentTimeMillis() / 1000;
        kickIfDiedTooMuch();
        // resetting death day
        resetDeathDay();
    }

    public void newDeathDay(long endTime) {
        final var diff = endTime - System.currentTimeMillis() / 1000; // time passed
        if (diff <= 0) {
            DifficultyDeathScaler.LOGGER.info("Death day is already passed, skipping");
            return;
        }
        deathDay++;
        deathDayEnd.add(endTime);
        scheduleDeathDayTask(diff * 20);
    }

    @Override
    protected void updateModifiersValue(List<Modifier<?>> modifiers) {
        modifiers.forEach(m -> {
            if (m instanceof final HealthModifier mod) {
                healthModifier = mod.getValue();
                if (player != null) mod.apply(player);
            }/* else if (m instanceof final LuckModifier mod) {
                luckModifier = mod.getValue();
                mod.apply(player);
            } */ else if (m instanceof final BlockBreakSpeedModifier mod) {
                blockBreakSpeedModifier = mod.getValue();
                if (player != null) mod.apply(player);
            } else if (m instanceof final MovementSpeedModifier mod) {
                movementSpeedModifier = mod.getValue();
                if (player != null) mod.apply(player);
            }
        });
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
        final var heartAmount = (20 + healthModifier + globalManager.getHealthModifier()) / 2;

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

        sb.append(generateFooterUpdate(STEPS, "you didn't die", updateType));

        return sb.toString();
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        applyModifiers();
    }

    public void save() {
        assert player != null || uuid != null;
        // get state
        PlayerData state;
        if (player == null) {
            DifficultyDeathScaler.LOGGER.info("Saving player with uuid {} difficulty data", uuid);
            state = StateSaver.getPlayerState(server, uuid);
        } else {
            DifficultyDeathScaler.LOGGER.info("Saving player ({}) difficulty data", player.getUuid());
            state = StateSaver.getPlayerState(player);
        }
        // save state
        save(state);

        state.bannedSince = bannedSince;
        final var ends = new long[deathDay];
        for (int i = 0; i < deathDay; i++) ends[i] = deathDayEnd.get(i);
        state.deathDayEnd = ends;
    }

    public void applyModifiers() {
        assert player != null;
        DifficultyDeathScaler.LOGGER.info("Applying modifier to player {}; health: {}", player.getName().getString(), healthModifier);
        HealthModifier.apply(player, healthModifier);
//        LuckModifier.apply(player, luckModifier);
        BlockBreakSpeedModifier.apply(player, blockBreakSpeedModifier);
        MovementSpeedModifier.apply(player, movementSpeedModifier);
    }

    public void setDeathDay(int n) {
        if (deathDay == n) return;
        final var t = 20 * 60 * 60 + System.currentTimeMillis() / 1000;
        if (n > deathDay) {
            for (int i = 0; i < n - deathDay; i++) newDeathDay(t);
        } else {
            resetDeathDay();
            for (int i = 0; i < n; i++) newDeathDay(t);
        }
        kickIfDiedTooMuch();
    }

    private void scheduleDeathDayTask(long ticksDelay) {
        final var task = new TickTask(() -> {
            if (deathDay != 0) {
                deathDay--;
                deathDayTasks.removeFirst();
            } else DifficultyDeathScaler.LOGGER.warn("Death day is already equal to 0");
        }, ticksDelay);
        timer.dds_runTask(task);
        deathDayTasks.add(task);
    }

    private void resetDeathDay() {
        deathDay = 0;
        deathDayTasks.forEach(TickTask::cancel);
        deathDayTasks.clear();
        deathDayEnd.clear();
    }

    public boolean diedTooMuch() {
        if (!ENABLE_TEMP_BAN) return false;
        return deathDay >= DEATH_BEFORE_TEMP_BAN ||
                (tempBan && System.currentTimeMillis() / 1000 - bannedSince < TEMP_BAN_DURATION);
    }

    /**
     * @throws IllegalStateException if the player is not temp banned
     */
    public Text getKickedDiedTooMuchMessage() {
        if (!tempBan) throw new IllegalStateException("Player is not temp banned");
        final var banTime = System.currentTimeMillis() / 1000 - bannedSince;
        final var banLength = TEMP_BAN_DURATION * 60 * 60L;
        return MutableText.of(new PlainTextContent.Literal("You died too much during 24h...\nYou can log back in "))
                .append(formatSeconds(banLength - banTime))
                .append(".");
    }

    /**
     * Kick the player if he died too much
     *
     * @return true if the player was kicked
     * @throws IllegalStateException if player is null
     */
    public boolean kickIfDiedTooMuch() {
        if (player == null) throw new IllegalStateException("Player is null");
        return kickIfDiedTooMuch(player.networkHandler);
    }

    /**
     * Kick the player if he died too much
     *
     * @return true if the player was kicked
     */
    public boolean kickIfDiedTooMuch(ServerPlayNetworkHandler handler) {
        if (diedTooMuch()) {
            if (!tempBan) {
                DifficultyDeathScaler.LOGGER.warn("Not banned because player was not temp banned. Death day were reset. Caused by an update?");
                resetDeathDay();
                return false;
            }
            handler.disconnect(getKickedDiedTooMuchMessage());
            return true;
        } else if (tempBan) {
            tempBan = false;
            bannedSince = -1;
        }
        return false;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        sb.append("PlayerDifficultyManager(uuid=");
        if (uuid == null) sb.append("null");
        else sb.append(uuid);
        sb.append(", number of death=")
                .append(numberOfDeath)
                .append(", banned since=")
                .append(bannedSince)
                .append(", total of death=")
                .append(totalOfDeath)
                .append(", death day=")
                .append(deathDay)
                .append(") {health modifier=")
                .append(healthModifier)
//                .append(", luck modifier=")
//                .append(luckModifier)
                .append(", block break speed modifier=")
                .append(blockBreakSpeedModifier)
                .append(", movement speed modifier=")
                .append(movementSpeedModifier)
                .append("}");
        return sb.toString();
    }

    public static class HealthModifier extends PlayerHealthModifier {
        public static final Identifier ID = Identifier.of(PREFIX + "player_health_modifier");

        public HealthModifier() {
            super(ID);
        }

        public static void apply(ServerPlayerEntity player, double value) {
            apply(ID, ATTRIBUTE, OPERATION, player, value);
        }
    }
}
