package world.anhgelus.architectsland.difficultydeathscaler.utils;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.concurrent.Callable;

public class MobUtils {
    public static void commonBetterGoals(HostileEntity e, GoalSelector targetSelector) {
        for (Goal g : targetSelector.getGoals()) {
            if (g instanceof RevengeGoal) {
                targetSelector.remove(g);
            }
        }
        // kill player become more important than defending itself
        targetSelector.add(2, new RevengeGoal(e));
        targetSelector.add(1, new ActiveTargetGoal<>(e, PlayerEntity.class, true));
    }

    public static void customSpawn(float proba, float max, Callable<Object> exec) {
        final var difficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get();
        if (difficulty == null) return;
        if (difficulty.getNumberOfDeath() >= 40) proba = max;
        try {
            if (Getters.RANDOM.nextFloat() * 100 < proba) exec.call();
        } catch (Exception e) {
            DifficultyDeathScaler.LOGGER.error("An error occurred while executing the custom spawn", e);
        }
    }
}
