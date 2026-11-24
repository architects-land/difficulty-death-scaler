package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class PassiveSpeedModifier extends PassiveModifier<Monster> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.MOVEMENT_SPEED;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
	public static final Identifier ID = DifficultyDeathScaler.id("speed");

	private PassiveSpeedModifier() {
		super(ID, ATTRIBUTE, OPERATION);
	}

	public static void apply(Monster entity, double value) {
		apply(ID, ATTRIBUTE, OPERATION, entity, value);
	}
}