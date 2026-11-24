package world.anhgelus.architectsland.difficultydeathscaler.passive.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;

public class PassiveModifier<T extends Monster> extends Modifier<T> {
	protected PassiveModifier(Identifier id, Holder<Attribute> attribute, AttributeModifier.Operation operation) {
		super(id, attribute, operation, Check.SMALLER);
	}
}
