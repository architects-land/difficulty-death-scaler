package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class BlockBreakSpeedModifier extends Modifier<ServerPlayerEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.BLOCK_BREAK_SPEED;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE;
    protected static Identifier ID = Identifier.of(PREFIX + "block_break_speed_modifier");

    public BlockBreakSpeedModifier() {
        super(ID, ATTRIBUTE, OPERATION, Check.SMALLER);
    }

    public static void apply(ServerPlayerEntity player, double value) {
        apply(ID, ATTRIBUTE, OPERATION, player, value);
    }
}
