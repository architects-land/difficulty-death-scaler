package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class SpawnReinforcementsModifier extends Modifier<HostileEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.SPAWN_REINFORCEMENTS;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    protected static final Identifier ID = Identifier.of(PREFIX + "spawn_reinforcements_modifier");

    public SpawnReinforcementsModifier() {
        super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
    }

    public static void apply(ZombieEntity player, double value) {
        apply(ID, ATTRIBUTE, OPERATION, player, value);
    }
}
