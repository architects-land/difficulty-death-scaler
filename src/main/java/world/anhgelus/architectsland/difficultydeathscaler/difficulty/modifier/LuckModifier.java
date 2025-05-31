package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * @deprecated Sadly minecraft does not use this value... In addition to this, if we want to use it, we must modify the <i>base</i> value
 */
@Deprecated
public class LuckModifier extends Modifier<ServerPlayerEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.LUCK;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    protected static final Identifier ID = Identifier.of(PREFIX + "luck_modifier");

    public LuckModifier() {
        super(ID, ATTRIBUTE, OPERATION, Check.SMALLER);
    }

    public static void apply(ServerPlayerEntity player, double value) {
        apply(ID, ATTRIBUTE, OPERATION, player, value);
    }
}
