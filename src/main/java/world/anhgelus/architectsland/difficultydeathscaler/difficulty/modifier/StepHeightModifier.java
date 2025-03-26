package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class StepHeightModifier extends Modifier<HostileEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.STEP_HEIGHT;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_VALUE;
    protected static final Identifier ID = Identifier.of(PREFIX + "step_height_modifier");

    public StepHeightModifier() {
        super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
    }

    public static void apply(HostileEntity player, double value) {
        apply(ID, ATTRIBUTE, OPERATION, player, value);
    }
}
