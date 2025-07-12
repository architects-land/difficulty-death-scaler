package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class BountyCriterion extends AbstractCriterion<BountyCriterion.Conditions> {
    @Override
    public Codec<BountyCriterion.Conditions> getConditionsCodec() {
        return BountyCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int id) {
        trigger(player, cond -> cond.requirementsMet(id));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate,
                             int id) implements AbstractCriterion.Conditions {

        public static Codec<BountyCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(BountyCriterion.Conditions::player),
                Codec.INT.fieldOf("id").forGetter(BountyCriterion.Conditions::id)
        ).apply(instance, BountyCriterion.Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int id) {
            return id == this.id;
        }
    }
}
