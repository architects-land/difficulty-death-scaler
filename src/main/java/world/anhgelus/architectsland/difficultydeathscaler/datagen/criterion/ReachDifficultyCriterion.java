package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class ReachDifficultyCriterion extends SimpleCriterionTrigger<ReachDifficultyCriterion.Conditions> {
	@Override
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, int difficulty) {
		trigger(player, cond -> cond.requirementsMet(difficulty));
	}

	public record Conditions(Optional<Holder<LootItemCondition>> playerPredicate,
	                         int min, int max) implements SimpleCriterionTrigger.SimpleInstance {

		public static Codec<ReachDifficultyCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
			Codec.INT.fieldOf("min").forGetter(Conditions::min),
			Codec.INT.fieldOf("max").forGetter(Conditions::max)
		).apply(instance, Conditions::new));

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return playerPredicate;
		}

		public boolean requirementsMet(int difficulty) {
			return max < 0 ? difficulty >= min : difficulty >= min && difficulty < max;
		}
	}
}
