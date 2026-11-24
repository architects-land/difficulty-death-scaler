package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ArbitraryCriterion extends SimpleCriterionTrigger<ArbitraryCriterion.Conditions> {
	@Override
	public @NonNull Codec<ArbitraryCriterion.Conditions> codec() {
		return ArbitraryCriterion.Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, int id) {
		trigger(player, cond -> cond.requirementsMet(id));
	}

	public record Conditions(Optional<Holder<LootItemCondition>> playerPredicate,
	                         int id) implements SimpleCriterionTrigger.SimpleInstance {

		public static Codec<ArbitraryCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LootItemCondition.CODEC.optionalFieldOf("player").forGetter(ArbitraryCriterion.Conditions::player),
			Codec.INT.fieldOf("id").forGetter(ArbitraryCriterion.Conditions::id)
		).apply(instance, ArbitraryCriterion.Conditions::new));

		@Override
		public @NonNull Optional<Holder<LootItemCondition>> player() {
			return playerPredicate;
		}

		public boolean requirementsMet(int id) {
			return id == this.id;
		}
	}
}
