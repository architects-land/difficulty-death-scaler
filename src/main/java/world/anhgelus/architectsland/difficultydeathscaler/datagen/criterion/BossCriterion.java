package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class BossCriterion extends SimpleCriterionTrigger<BossCriterion.Conditions> {
	@Override
	public Codec<BossCriterion.Conditions> codec() {
		return BossCriterion.Conditions.CODEC;
	}

	public void trigger(ServerPlayer player, Class<? extends Entity> entity, boolean killed) {
		trigger(player, cond -> cond.requirementsMet(entity, killed));
	}

	public record Conditions(Optional<Holder<LootItemCondition>> playerPredicate,
	                         String entity,
	                         boolean killed) implements SimpleCriterionTrigger.SimpleInstance {

		public static Codec<BossCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LootItemCondition.CODEC.optionalFieldOf("player").forGetter(BossCriterion.Conditions::player),
			Codec.STRING.fieldOf("entity").forGetter(BossCriterion.Conditions::entity),
			Codec.BOOL.fieldOf("killed").forGetter(BossCriterion.Conditions::killed)
		).apply(instance, BossCriterion.Conditions::new));

		@Override
		public Optional<Holder<LootItemCondition>> player() {
			return playerPredicate;
		}

		public boolean requirementsMet(Class<? extends Entity> entity, boolean killed) {
			return entity.getName().equals(this.entity) && killed == this.killed;
		}
	}
}
