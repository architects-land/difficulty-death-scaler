package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.Difficulty;

public class PlayerHealthModifier extends Difficulty.IntegerModifier {
    public static Identifier MODIFIER_ID = Identifier.of("death_difficulty_health_modifier");;

    @Override
    public void update(Integer newValue) {
        if (newValue < value) value = newValue;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        final var healthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttribute == null) return;

        healthAttribute.removeModifier(MODIFIER_ID);
        if (value == 0) return;

        EntityAttributeModifier playerHealthModifier = new EntityAttributeModifier(
                MODIFIER_ID, value, EntityAttributeModifier.Operation.ADD_VALUE
        );
        healthAttribute.addPersistentModifier(playerHealthModifier);
    }
}