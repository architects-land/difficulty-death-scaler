package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PassiveMovementEfficiencyModifier extends PassiveModifier<HostileEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.MOVEMENT_EFFICIENCY;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_VALUE;
    public static final Identifier ID = Identifier.of(PREFIX + "movement_efficiency");

    private PassiveMovementEfficiencyModifier() {
        super(ID, ATTRIBUTE, OPERATION);
    }

    public static void apply(HostileEntity entity, double value) {
        apply(ID, ATTRIBUTE, OPERATION, entity, value);
    }
}