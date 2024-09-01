package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;

public class PLayerSteps {
    /**
     * Default step, is always reached
     */
    public static class Default extends DifficultyManager.Step {
        protected Default() {
            super(0);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(0);
        }
    }

    public static class First extends DifficultyManager.Step {
        protected First() {
            super(3);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-2);
        }
    }

    public static class Second extends DifficultyManager.Step {
        protected Second() {
            super(5);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-4);
        }
    }

    public static class Third extends DifficultyManager.Step {
        protected Third() {
            super(7);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-6);
        }
    }

    public static class Fourth extends DifficultyManager.Step {
        protected Fourth() {
            super(10);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-8);
        }
    }

    public static class Fifth extends DifficultyManager.Step {
        protected Fifth() {
            super(15);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, DifficultyManager.Updater updater) {
            final DifficultyManager.Modifier modifier = updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-10);
        }
    }
}
