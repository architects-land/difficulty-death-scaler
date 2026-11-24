package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class BountyCriterion extends SimpleCriterionTrigger<BountyCriterion.Conditions> {
	@Override
	public Codec<BountyCriterion.Conditions> codec() {
		return BountyCriterion.Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, int id) {
		trigger(player, cond -> cond.requirementsMet(id));
	}

	public record Conditions(Optional<Holder<LootItemCondition>> playerPredicate,
	                         int id) implements SimpleCriterionTrigger.SimpleInstance {

		public static Codec<BountyCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LootItemCondition.CODEC.optionalFieldOf("player").forGetter(BountyCriterion.Conditions::player),
			Codec.INT.fieldOf("id").forGetter(BountyCriterion.Conditions::id)
		).apply(instance, BountyCriterion.Conditions::new));

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return playerPredicate;
		}

		public boolean requirementsMet(int id) {
			return id == this.id;
		}
	}
}
