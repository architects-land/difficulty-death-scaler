package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class Boss {
    private final LivingEntity entity;
    private final CustomBehavior customBehavior;

    @FunctionalInterface
    public interface CustomBehavior {
        void apply(LivingEntity entity);
    }

    public Boss(LivingEntity entity) {
        this.entity = entity;
        this.customBehavior = (e) -> {
        };
    }

    public Boss(LivingEntity entity, CustomBehavior behavior) {
        this.entity = entity;
        this.customBehavior = behavior;
    }

    public void buff() {
        buffAttribute(entity, EntityAttributes.KNOCKBACK_RESISTANCE, "dds_kb", buffedKnockback(), EntityAttributeModifier.Operation.ADD_VALUE);
        buffAttribute(entity, EntityAttributes.MAX_HEALTH, "dds_health", buffedHealth(), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        buffAttribute(entity, EntityAttributes.ATTACK_DAMAGE, "dds_damage", buffedDamage(), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        buffAttribute(entity, EntityAttributes.SCALE, "dds_scale", buffedScale(), EntityAttributeModifier.Operation.ADD_VALUE);
        buffAttribute(entity, EntityAttributes.MOVEMENT_SPEED, "dds_speed", buffedSpeed(), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        customBehavior.apply(entity);
        DifficultyDeathScaler.LOGGER.info("{} buffed", this);
    }

    private float buffedKnockback() {
        if (entity instanceof ElderGuardianEntity || entity instanceof WardenEntity) {
            return 0.8f;
        }
        return 0;
    }

    private float buffedHealth() {
        if (entity instanceof ElderGuardianEntity) {
            return 2f;
        }
        return 0;
    }

    private float buffedSpeed() {
        if (entity instanceof ElderGuardianEntity) {
            return 2;
        } else if (entity instanceof WardenEntity) {
            return 1.5f;
        }
        return 0;
    }

    private float buffedScale() {
        if (entity instanceof ElderGuardianEntity) {
            return -0.33f;
        }
        return 0;
    }

    private float buffedDamage() {
        if (entity instanceof ElderGuardianEntity) {
            return 6;
        }
        return 0;
    }

    public static void buffAttribute(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, String id, float value, EntityAttributeModifier.Operation operation) {
        final var attr = entity.getAttributeInstance(attribute);
        if (attr != null) {
            final var modifier = new EntityAttributeModifier(Identifier.of(id), value, operation);
            attr.addTemporaryModifier(modifier);
        }
    }

    public static Boss fromEntity(LivingEntity entity, @Nullable DragonBuff dragonBuff) {
        if (entity instanceof WitherEntity) {
            return new Boss(entity, (e) -> {
                final var world = e.getWorld();

                final var hitResult = world.raycast(new RaycastContext(
                        e.getPos(),
                        e.getPos().add(0, 4, 0),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        e
                ));

                if (hitResult.getType() != HitResult.Type.BLOCK) return;
                if (world.getBlockState(hitResult.getBlockPos()).getBlock() != Blocks.BEDROCK) return;

                e.setPosition(e.getPos().add(0, -2, 0));
            });
        }
        return new Boss(entity);
    }

    @Override
    public String toString() {
        return String.format(
                "Boss(entity uuid=%s, entity class=%s, entity location=%s)",
                entity.getUuid(), entity.getClass().getSimpleName(), entity.getPos().toString()
        );
    }
}
