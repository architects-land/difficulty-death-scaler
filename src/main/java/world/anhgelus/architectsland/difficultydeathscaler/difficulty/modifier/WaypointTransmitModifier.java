package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class WaypointTransmitModifier extends Modifier<ServerPlayer> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.WAYPOINT_TRANSMIT_RANGE;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_VALUE;
	public static final double BASE_VALUE = 60_000_000;
	protected static final Identifier ID = DifficultyDeathScaler.id("waypoint_receive_modifier");

	public WaypointTransmitModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.SMALLER);
	}

	public static void apply(ServerPlayer player, double value) {
		// works like a set
		apply(ID, ATTRIBUTE, OPERATION, player, -BASE_VALUE + value);
	}
}
