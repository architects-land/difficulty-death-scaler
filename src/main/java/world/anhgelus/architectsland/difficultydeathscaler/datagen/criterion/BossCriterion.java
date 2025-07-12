package world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class BossCriterion extends AbstractCriterion<BossCriterion.Conditions> {
    @Override
    public Codec<BossCriterion.Conditions> getConditionsCodec() {
        return BossCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Class<? extends Entity> entity, boolean killed) {
        trigger(player, cond -> cond.requirementsMet(entity, killed));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate,
                             String entity, boolean killed) implements AbstractCriterion.Conditions {

        public static Codec<BossCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(BossCriterion.Conditions::player),
                Codec.STRING.fieldOf("entity").forGetter(BossCriterion.Conditions::entity),
                Codec.BOOL.fieldOf("killed").forGetter(BossCriterion.Conditions::killed)
        ).apply(instance, BossCriterion.Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(Class<? extends Entity> entity, boolean killed) {
            return entity.getName().equals(this.entity) && killed == this.killed;
        }
    }
}
