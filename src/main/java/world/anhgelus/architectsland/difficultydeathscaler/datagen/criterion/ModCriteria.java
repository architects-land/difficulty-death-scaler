package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class ModCriteria {
	public static final ReachDifficultyCriterion REACH_GLOBAL_DIFFICULTY = register("global_difficulty", new ReachDifficultyCriterion());
	public static final ReachDifficultyCriterion REACH_PLAYER_DIFFICULTY = register("player_difficulty", new ReachDifficultyCriterion());

	public static final ArbitraryCriterion ARBITRARY = register("arbitrary", new ArbitraryCriterion());

	public static final BossCriterion BOSS = register("warden_buffed", new BossCriterion());

	public static final BountyCriterion BOUNTY = register("bounty", new BountyCriterion());

	public static void init() {
	}

	private static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
		return Registry.register(BuiltInRegistries.TRIGGER_TYPES, DifficultyDeathScaler.id(name), criterion);
	}
}
