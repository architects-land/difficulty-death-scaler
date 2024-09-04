package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;

public class FallDamageMultiplierModifier extends DifficultyManager.Modifier<ServerPlayerEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    protected static Identifier ID = Identifier.of(PREFIX + "fall_damage_multiplier_modifier");

    public FallDamageMultiplierModifier() {
        super(ID, ATTRIBUTE, OPERATION);
    }

    @Override
    public void update(double newValue) {
        if (newValue > value) value = newValue;
    }

    public static void apply(HostileEntity player, double value) {
        apply(ID, ATTRIBUTE, OPERATION, player, value);
    }
}
