package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class StepHeightModifier extends Modifier<Monster> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.STEP_HEIGHT;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_VALUE;
	protected static final Identifier ID = DifficultyDeathScaler.id("step_height_modifier");

	public StepHeightModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
	}

	public static void apply(Monster player, double value) {
		apply(ID, ATTRIBUTE, OPERATION, player, value);
	}
}
