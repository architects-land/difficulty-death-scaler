package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

public class Boss {
	private final LivingEntity entity;
	private final CustomBehavior customBehavior;

	public Boss(LivingEntity entity) {
		this.entity = entity;
		this.customBehavior = (e) -> {
		};
	}

	public Boss(LivingEntity entity, CustomBehavior behavior) {
		this.entity = entity;
		this.customBehavior = behavior;
	}

	public static void buffAttribute(LivingEntity entity, Holder<Attribute> attribute, String id, float value, AttributeModifier.Operation operation) {
		final var attr = entity.getAttribute(attribute);
		if (attr != null) {
			final var modifier = new AttributeModifier(Identifier.parse(id), value, operation);
			attr.addTransientModifier(modifier);
		}
	}

	public static Boss fromEntity(LivingEntity entity, @Nullable DragonBuff dragonBuff) {
		if (entity instanceof WitherBoss) {
			return new Boss(entity, (e) -> {
				final var world = e.level();

				final var hitResult = world.clip(new ClipContext(
					e.getPosition(0),
					e.getPosition(0).add(0, 4, 0),
					ClipContext.Block.COLLIDER,
					ClipContext.Fluid.NONE,
					e
				));

				if (hitResult.getType() != HitResult.Type.BLOCK) return;
				if (world.getBlockState(hitResult.getBlockPos()).getBlock() != Blocks.BEDROCK)
					return;

				e.setPos(e.getPosition(0).add(0, -2, 0));
			});
		}
		return new Boss(entity);
	}

	public void buff() {
		buffAttribute(entity, Attributes.KNOCKBACK_RESISTANCE, "dds_kb", buffedKnockback(), AttributeModifier.Operation.ADD_VALUE);
		buffAttribute(entity, Attributes.MAX_HEALTH, "dds_health", buffedHealth(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		buffAttribute(entity, Attributes.ATTACK_DAMAGE, "dds_damage", buffedDamage(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		buffAttribute(entity, Attributes.SCALE, "dds_scale", buffedScale(), AttributeModifier.Operation.ADD_VALUE);
		buffAttribute(entity, Attributes.MOVEMENT_SPEED, "dds_speed", buffedSpeed(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		customBehavior.apply(entity);
		DifficultyDeathScaler.LOGGER.info("{} buffed", this);
	}

	private float buffedKnockback() {
		if (entity instanceof ElderGuardian || entity instanceof Warden) {
			return 0.8f;
		}
		return 0;
	}

	private float buffedHealth() {
		if (entity instanceof ElderGuardian) {
			return 2f;
		}
		return 0;
	}

	private float buffedSpeed() {
		if (entity instanceof ElderGuardian) {
			return 2;
		} else if (entity instanceof Warden) {
			return 1.5f;
		}
		return 0;
	}

	private float buffedScale() {
		if (entity instanceof ElderGuardian) {
			return -0.33f;
		}
		return 0;
	}

	private float buffedDamage() {
		if (entity instanceof ElderGuardian) {
			return 6;
		}
		return 0;
	}

	@Override
	public String toString() {
		return String.format(
			"Boss(entity uuid=%s, entity class=%s, entity location=%s)",
			entity.getUUID(), entity.getClass().getSimpleName(), entity.getPosition(0)
		);
	}

	@FunctionalInterface
	public interface CustomBehavior {
		void apply(LivingEntity entity);
	}
}
