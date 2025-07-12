package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class WaypointTransmitModifier extends Modifier<ServerPlayerEntity> {
    public static final RegistryEntry<EntityAttribute> ATTRIBUTE = EntityAttributes.WAYPOINT_TRANSMIT_RANGE;
    public static final EntityAttributeModifier.Operation OPERATION = EntityAttributeModifier.Operation.ADD_VALUE;
    public static final double BASE_VALUE = 60_000_000;
    protected static final Identifier ID = Identifier.of(PREFIX + "waypoint_receive_modifier");

    public WaypointTransmitModifier() {
        super(ID, ATTRIBUTE, OPERATION, Check.SMALLER);
    }

    public static void apply(ServerPlayerEntity player, double value) {
        // works like a set
        apply(ID, ATTRIBUTE, OPERATION, player, -BASE_VALUE + value);
    }
}
