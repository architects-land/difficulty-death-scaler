package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.Difficulty;

public class GlobalSteps {
    /**
     * Default step, is always reached
     */
    public static class Default extends Difficulty.Step {
        protected Default() {
            super(0);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(30, server);
            gamerules.get(GameRules.NATURAL_REGENERATION).set(true, server);
            updater.updateDifficulty(2);
        }
    }

    public static class First extends Difficulty.Step {
        protected First() {
            super(1);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(70, server);
        }
    }

    public static class Second extends Difficulty.Step {
        protected Second() {
            super(3);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            updater.updateDifficulty(Difficulty.Updater.DIFFICULTY_LEVEL.get(net.minecraft.world.Difficulty.HARD));
        }
    }

    public static class Third extends Difficulty.Step {
        protected Third() {
            super(5);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            gamerules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(100, server);
        }
    }

    public static class Fourth extends Difficulty.Step {
        protected Fourth() {
            super(7);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            final Difficulty.IntegerModifier modifier = (Difficulty.IntegerModifier) updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-2);
        }
    }

    public static class Fifth extends Difficulty.Step {
        protected Fifth() {
            super(10);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            final Difficulty.IntegerModifier modifier = (Difficulty.IntegerModifier) updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-4);
        }
    }

    public static class Sixth extends Difficulty.Step {
        protected Sixth() {
            super(12);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            final Difficulty.IntegerModifier modifier = (Difficulty.IntegerModifier) updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-6);
        }
    }

    public static class Seventh extends Difficulty.Step {
        protected Seventh() {
            super(15);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            final Difficulty.IntegerModifier modifier = (Difficulty.IntegerModifier) updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-8);
        }
    }

    public static class Eighth extends Difficulty.Step {
        protected Eighth() {
            super(17);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            final Difficulty.IntegerModifier modifier = (Difficulty.IntegerModifier) updater.getModifier(PlayerHealthModifier.class);
            modifier.update(-10);
        }
    }

    public static class Ninth extends Difficulty.Step {
        protected Ninth() {
            super(20);
        }

        @Override
        public void reached(MinecraftServer server, GameRules gamerules, Difficulty.Updater updater) {
            gamerules.get(GameRules.NATURAL_REGENERATION).set(false, server);
        }
    }
}
