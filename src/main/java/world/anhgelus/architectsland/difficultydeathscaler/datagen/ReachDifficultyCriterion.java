package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ReachDifficultyCriterion extends AbstractCriterion<ReachDifficultyCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int difficulty) {
        trigger(player, cond -> cond.requirementsMet(difficulty));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate,
                             int requiredDifficulty) implements AbstractCriterion.Conditions {

        public static Codec<ReachDifficultyCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.INT.fieldOf("requiredDifficulty").forGetter(Conditions::requiredDifficulty)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int difficulty) {
            return difficulty >= requiredDifficulty;
        }
    }
}
