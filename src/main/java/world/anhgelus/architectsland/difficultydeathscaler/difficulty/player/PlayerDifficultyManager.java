package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.StateSaver;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.PlayerHealthModifier;

import java.util.List;

public class PlayerDifficultyManager extends DifficultyManager {
    public ServerPlayerEntity player;

    public static final int SECONDS_BEFORE_DECREASED = 12*60*60;

    public static class HealthModifier extends PlayerHealthModifier {
        public static final Identifier ID = Identifier.of(PREFIX + "player_health_modifier");

        static {
            IDENTIFIER = ID;
        }

        public HealthModifier() {
            super(ID);
        }
    }

    public static final StepPair[] STEPS = new StepPair[]{
            new StepPair(0, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(0);
            }),
            new StepPair(3, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-2);
            }),
            new StepPair(5, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-4);
            }),
            new StepPair(7, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-6);
            }),
            new StepPair(10, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-8);
            }),
            new StepPair(15, (server, gamerules, updater) -> {
                updater.getModifier(HealthModifier.class).update(-10);
            }),
    };

    protected int healthModifierValue = 0;

    public PlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);
        this.player = player;

        DifficultyDeathScaler.LOGGER.info("Loading player {} difficulty data", player.getUuid());
        final var state = StateSaver.getPlayerState(player);
        numberOfDeath = state.deaths;
        delayFirstTask(state.timeBeforeReduce);

        updateModifiersValue(modifiers(numberOfDeath));
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        updateModifiersValue(updater);

        player.sendMessage(Text.of(generateDifficultyUpdate(updateType, updater.getDifficulty())), false);

        playSoundUpdate(updateType, player);
    }

    @Override
    protected void updateModifiersValue(List<Modifier> modifiers) {
        modifiers.forEach(m -> {
            if (m instanceof final HealthModifier hm) healthModifierValue = (int) hm.getValue();
            m.apply(player);
        });
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
        final var heartAmount = (20 + healthModifierValue) / 2;

        final var sb = new StringBuilder();
        sb.append(generateHeaderUpdate(updateType));

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
        DifficultyDeathScaler.LOGGER.info("Saving player {} difficulty data", player.getUuid());
        final var state = StateSaver.getPlayerState(player);
        state.deaths = numberOfDeath;
        state.timeBeforeReduce = delay();
    }

    public void applyModifiers() {
        HealthModifier.apply(player, healthModifierValue);
    }
}
