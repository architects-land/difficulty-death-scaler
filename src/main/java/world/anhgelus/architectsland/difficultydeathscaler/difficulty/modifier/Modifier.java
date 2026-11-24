package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class Modifier<T extends LivingEntity> {
	protected final Identifier id;
	protected final Holder<Attribute> attribute;
	protected final AttributeModifier.Operation operation;
	protected final Check check;
	protected double value = 0;
	protected boolean valueSet = false;

	protected Modifier(Identifier id, Holder<Attribute> attribute, AttributeModifier.Operation operation, Check check) {
		this.id = id;
		this.attribute = attribute;
		this.operation = operation;
		this.check = check;
	}

	public static void apply(
		Identifier id,
		Holder<Attribute> attribute,
		AttributeModifier.Operation operation,
		LivingEntity entity,
		double value
	) {
		final var attr = entity.getAttribute(attribute);
		if (attr == null) {
			DifficultyDeathScaler.LOGGER.warn("Attribute {} not found for {}", attribute, entity.getClass().getSimpleName());
			return;
		}

		attr.removeModifier(id);
		if (value == 0) return;

		final var playerHealthModifier = new AttributeModifier(id, value, operation);
		attr.addPermanentModifier(playerHealthModifier);
	}

	/**
	 * Update the value if needed
	 *
	 * @param newValue newValue
	 */
	public void update(double newValue) {
		if (!valueSet) {
			value = newValue;
			valueSet = true;
			return;
		}
		if (check == Check.SMALLER && newValue < value) value = newValue;
		else if (check == Check.BIGGER && newValue > value) value = newValue;
	}

	/**
	 * Apply modifier to player
	 *
	 * @param entity Entity to apply the modifier
	 */
	public void apply(@Nullable LivingEntity entity) {
		if (entity == null) return;
		apply(id, attribute, operation, entity, value);
	}

	public double getValue() {
		return value;
	}

	protected enum Check {
		BIGGER, SMALLER
	}
}
