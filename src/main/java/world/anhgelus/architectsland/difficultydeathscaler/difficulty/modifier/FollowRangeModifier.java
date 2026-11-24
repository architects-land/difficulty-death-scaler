package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class FollowRangeModifier extends Modifier<Monster> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.FOLLOW_RANGE;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
	protected static final Identifier ID = DifficultyDeathScaler.id("follow_range_modifier");

	public FollowRangeModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
	}

	public static void apply(Monster entity, double value) {
		apply(ID, ATTRIBUTE, OPERATION, entity, value);
	}
}