package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class SpawnReinforcementsModifier extends Modifier<Monster> {
	public static final Holder<Attribute> ATTRIBUTE = Attributes.SPAWN_REINFORCEMENTS_CHANCE;
	public static final AttributeModifier.Operation OPERATION = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
	protected static final Identifier ID = DifficultyDeathScaler.id("spawn_reinforcements_modifier");

	public SpawnReinforcementsModifier() {
		super(ID, ATTRIBUTE, OPERATION, Check.BIGGER);
	}

	public static void apply(Zombie player, double value) {
		apply(ID, ATTRIBUTE, OPERATION, player, value);
	}
}
