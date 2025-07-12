package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ArbitraryCriterion extends AbstractCriterion<ArbitraryCriterion.Conditions> {
    @Override
    public Codec<ArbitraryCriterion.Conditions> getConditionsCodec() {
        return ArbitraryCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int id) {
        trigger(player, cond -> cond.requirementsMet(id));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate,
                             int id) implements AbstractCriterion.Conditions {

        public static Codec<ArbitraryCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(ArbitraryCriterion.Conditions::player),
                Codec.INT.fieldOf("id").forGetter(ArbitraryCriterion.Conditions::id)
        ).apply(instance, ArbitraryCriterion.Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int id) {
            return id == this.id;
        }
    }
}
