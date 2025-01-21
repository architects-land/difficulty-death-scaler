package world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class Modifier<T extends LivingEntity> {
    public static final String PREFIX = "dds_";

    protected enum Check {
        BIGGER, SMALLER
    }

    protected double value = 0;
    protected boolean valueSet = false;
    protected final Identifier id;
    protected final RegistryEntry<EntityAttribute> attribute;
    protected final EntityAttributeModifier.Operation operation;
    protected final Check check;

    protected Modifier(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, Check check) {
        this.id = id;
        this.attribute = attribute;
        this.operation = operation;
        this.check = check;
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
    public void apply(@Nullable T entity) {
        if (entity == null) return;
        apply(id, attribute, operation, entity, value);
    }

    protected static void apply(
            Identifier id,
            RegistryEntry<EntityAttribute> attribute,
            EntityAttributeModifier.Operation operation,
            LivingEntity entity,
            double value
    ) {
        final var attr = entity.getAttributeInstance(attribute);
        if (attr == null) return;

        attr.removeModifier(id);
        if (value == 0) return;

        final var playerHealthModifier = new EntityAttributeModifier(
                id, value, operation
        );
        attr.addPersistentModifier(playerHealthModifier);
    }

    public double getValue() {
        return value;
    }
}
