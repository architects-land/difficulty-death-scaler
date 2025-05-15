package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class PassiveHealthModifier extends PassiveModifier<HostileEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.MAX_HEALTH;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    public static final Identifier ID = Identifier.of(PREFIX + "health");

    private PassiveHealthModifier() {
        super(ID, ATTRIBUTE, OPERATION);
    }

    public static void apply(HostileEntity entity, double value) {
        apply(ID, ATTRIBUTE, OPERATION, entity, value);
    }
}