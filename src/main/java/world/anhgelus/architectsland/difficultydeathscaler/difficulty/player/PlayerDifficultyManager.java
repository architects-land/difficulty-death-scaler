package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;

public class PlayerDifficultyManager extends DifficultyManager {
    public final ServerPlayerEntity player;

    public static final int SECONDS_BEFORE_DECREASED = 12*60*60;

    public static final Step[] STEPS = new Step[]{
            new PLayerSteps.Default(),
            new PLayerSteps.First(),
            new PLayerSteps.Second(),
            new PLayerSteps.Third(),
            new PLayerSteps.Fourth(),
            new PLayerSteps.Fifth(),
    };

    protected int playerHealthModifierValue = 0;

    public PlayerDifficultyManager(MinecraftServer server, ServerPlayerEntity player) {
        super(server, STEPS, SECONDS_BEFORE_DECREASED);
        this.player = player;
        // load saved data
    }

    @Override
    protected void onUpdate(UpdateType updateType, Updater updater) {
        updater.getModifiers().forEach(m -> {
            if (m instanceof final PlayerHealthModifier phm) playerHealthModifierValue = (int) phm.getValue();
            m.apply(player);
        });
        playSoundUpdate(updateType, player);
    }

    @Override
    protected @NotNull String generateDifficultyUpdate(UpdateType updateType, @Nullable Difficulty difficulty) {
        return "";
    }

    @Override
    public void applyModifiers(ServerPlayerEntity player) {
        applyModifiers();
    }

    @Override
    public void save() {
        //
    }

    public void applyModifiers() {
        PlayerHealthModifier.apply(player, playerHealthModifierValue);
    }
}
