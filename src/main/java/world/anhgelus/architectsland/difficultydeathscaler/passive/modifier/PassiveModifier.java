package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;

public class PassiveModifier<T extends HostileEntity> extends Modifier<T> {
    public static final String PREFIX = Modifier.PREFIX + "passive_";

    protected PassiveModifier(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation) {
        super(id, attribute, operation, Check.SMALLER);
    }
}
