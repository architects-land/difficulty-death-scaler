package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class BlockBreakSpeedModifier extends Modifier<ServerPlayer> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.BLOCK_BREAK_SPEED;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
	protected static final Identifier ID = DifficultyDeathScaler.id("block_break_speed_modifier");

	public BlockBreakSpeedModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.SMALLER);
	}

	public static void apply(ServerPlayer player, double value) {
		apply(ID, ATTRIBUTE, OPERATION, player, value);
	}
}
