package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import net.minecraft.advancement.criterion.Criteria;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class ModCriteria {
    public static final ReachDifficultyCriterion REACH_GLOBAL_DIFFICULTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":global_difficulty", new ReachDifficultyCriterion());
    public static final ReachDifficultyCriterion REACH_PLAYER_DIFFICULTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":player_difficulty", new ReachDifficultyCriterion());

    public static void init() {
    }
}
