package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class PassiveKnockbackResistanceModifier extends PassiveModifier<Monster> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.KNOCKBACK_RESISTANCE;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_VALUE;
	public static final Identifier ID = DifficultyDeathScaler.id("knockback_resistance");

	private PassiveKnockbackResistanceModifier() {
		super(ID, ATTRIBUTE, OPERATION);
	}

	public static void apply(Monster entity, double value) {
		apply(ID, ATTRIBUTE, OPERATION, entity, value);
	}
}