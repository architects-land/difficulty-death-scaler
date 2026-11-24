package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class FallDamageMultiplierModifier extends Modifier<ServerPlayer> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.FALL_DAMAGE_MULTIPLIER;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
	protected static final Identifier ID = DifficultyDeathScaler.id("fall_damage_multiplier_modifier");

	public FallDamageMultiplierModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
	}

	public static void apply(ServerPlayer player, double value) {
		apply(ID, ATTRIBUTE, OPERATION, player, value);
	}
}
