package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import net.minecraft.advancement.criterion.Criteria;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class ModCriteria {
    public static final ReachDifficultyCriterion REACH_GLOBAL_DIFFICULTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":global_difficulty", new ReachDifficultyCriterion());
    public static final ReachDifficultyCriterion REACH_PLAYER_DIFFICULTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":player_difficulty", new ReachDifficultyCriterion());

    public static final ArbitraryCriterion ARBITRARY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":arbitrary", new ArbitraryCriterion());

    public static final BossCriterion BOSS = Criteria.register(DifficultyDeathScaler.MOD_ID + ":warden_buffed", new BossCriterion());

    public static final BountyCriterion BOUNTY = Criteria.register(DifficultyDeathScaler.MOD_ID + ":bounty", new BountyCriterion());

    public static void init() {
    }
}
