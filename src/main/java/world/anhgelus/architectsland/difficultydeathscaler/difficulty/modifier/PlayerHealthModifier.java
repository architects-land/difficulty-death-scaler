package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PlayerHealthModifier extends Modifier<ServerPlayer> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.MAX_HEALTH;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_VALUE;

	public PlayerHealthModifier(Identifier id) {
		super(id, ATTRIBUTE, OPERATION, Check.SMALLER);
	}

	public static void apply(ServerPlayer player, Identifier id, double value) {
		apply(id, ATTRIBUTE, OPERATION, player, value);
	}
}