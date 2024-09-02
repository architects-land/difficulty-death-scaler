package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;

public class PlayerDifficultyManager extends DifficultyManager {
    public ServerPlayerEntity player;

    public static final int SECONDS_BEFORE_DECREASED = 12*60*60;

    public static final StepPair[] STEPS = new StepPair[]{
            new StepPair(0, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(0);
            }),
            new StepPair(3, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(-2);
            }),
            new StepPair(5, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(-4);
            }),
            new StepPair(7, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(-6);
            }),
            new StepPair(10, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(-8);
            }),
            new StepPair(15, (server, gamerules, updater) -> {
                updater.getModifier(PlayerHealthModifier.class).update(-10);
            }),
    };

    protected int playerHealthModifierValue = 0;

    public PlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);
        this.player = player;

        DifficultyDeathScaler.LOGGER.info("Loading player {} difficulty data", player.getUuid());
        final var state = StateSaver.getPlayerState(player);
        numberOfDeath = state.deaths;
        delayFirstTask(state.timeBeforeReduce);
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        updater.getModifiers().forEach(m -> {
            if (m instanceof final PlayerHealthModifier phm) playerHealthModifierValue = (int) phm.getValue();
            m.apply(player);
        });

        player.sendMessage(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);

        playSoundUpdate(updateType, player);
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
        final var heartAmount = (20 + playerHealthModifierValue) / 2;

        final var sb = new StringBuilder();
        sb.append("Player max heart: ");
        if (heartAmount == 10) {
            sb.append("§2");
        } else if (heartAmount >= 8) {
            sb.append("§e");
        } else {
            sb.append("§c");
        }
        sb.append(heartAmount).append(" ❤§r\n");

        sb.append(generateFooterUpdate(STEPS, updateType));

        return sb.toString();
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        applyModifiers();
    }

    @Override
    public void save() {
        DifficultyDeathScaler.LOGGER.info("Saving player {} difficulty data", player.getUuid());
        final var state = StateSaver.getServerState(server);
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = System.currentTimeMillis() / 1000 - timerStart;
    }

    public void applyModifiers() {
        PlayerHealthModifier.apply(player, playerHealthModifierValue);
    }
}
