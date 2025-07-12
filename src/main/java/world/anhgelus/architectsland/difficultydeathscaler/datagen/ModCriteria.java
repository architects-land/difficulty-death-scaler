package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import net.minecraft.advancement.criterion.Criteria;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class ModCriteria {
    public static final ReachDifficultyCriterion REACH_DIFFICULTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":difficulty", new ReachDifficultyCriterion());

    public static void init() {
    }
}
