package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BossManager {
    public static final Item buffingItem = Items.NETHERITE_INGOT;

    private static final List<UUID> buffedBosses = new ArrayList<>();

    public static ActionResult handleBuff(PlayerEntity player, World world, Hand hand, Entity entity) {
        if (!(entity instanceof WitherEntity ||
                entity instanceof EnderDragonEntity ||
                entity instanceof ElderGuardianEntity ||
                entity instanceof WardenEntity)) return ActionResult.PASS;

        if (buffedBosses.contains(entity.getUuid())) return ActionResult.PASS;

        final ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(buffingItem)) return ActionResult.PASS;
        itemStack.decrementUnlessCreative(1, player);

        BuffableBoss<?> boss = switch (entity) {
            case WitherEntity witherEntity -> getBoss(witherEntity);
            case EnderDragonEntity enderDragonEntity -> getBoss(enderDragonEntity);
            case ElderGuardianEntity elderGuardianEntity -> getBoss(elderGuardianEntity);
            default -> getBoss((WardenEntity) entity);
        };
        boss.buff();
        buffedBosses.add(entity.getUuid());

        var living = (LivingEntity) entity;
        living.setHealth(living.getMaxHealth());

        final var lightingBolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightingBolt.setPosition(entity.getPos());

        world.spawnEntity(lightingBolt);
        living.setHealth(living.getMaxHealth());

        return ActionResult.SUCCESS;
    }

    public static void handleKill(Entity entity, DifficultyManager manager) {
        if (!(entity instanceof EnderDragonEntity) && !buffedBosses.contains(entity.getUuid())) {
            return;
        }
        buffedBosses.remove(entity.getUuid());
        if (manager.getNumberOfDeath() >= DifficultyManager.DEATH_STEPS[1]) {
            manager.decreaseDeath(entity.getServer());
        }
    }

    private static BuffableBoss<ElderGuardianEntity> getBoss(ElderGuardianEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {
                DifficultyDeathScaler.LOGGER.info("Elder Guardian buffed");

                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        "death_difficulty.elder_guardian.kb",
                        0.8f,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_MAX_HEALTH,
                        "death_difficulty.elder_guardian.health",
                        2,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        "death_difficulty.elder_guardian.damage",
                        6,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_SCALE,
                        "death_difficulty.elder_guardian.scale",
                        -0.33f,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        "death_difficulty.elder_guardian.speed",
                        2f,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
            }
        };
    }

    private static BuffableBoss<EnderDragonEntity> getBoss(EnderDragonEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {
                DifficultyDeathScaler.LOGGER.info("Ender Dragon buffed");
            }
        };
    }

    private static BuffableBoss<WardenEntity> getBoss(WardenEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {
                DifficultyDeathScaler.LOGGER.info("Warden buffed");

                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        "death_difficulty.warden.speed",
                        1.5f,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                buffAttribute(
                        entity,
                        EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                        "death_difficulty.warden.kb",
                        0.8f,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
            }
        };
    }

    private static BuffableBoss<WitherEntity> getBoss(WitherEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {
                DifficultyDeathScaler.LOGGER.info("Wither buffed");

                World world = entity.getWorld();

                BlockHitResult hitResult = world.raycast(new RaycastContext(
                        entity.getPos(),
                        entity.getPos().add(0, 4, 0),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        entity
                ));

                if (hitResult.getType() != HitResult.Type.BLOCK) return;
                if (world.getBlockState(hitResult.getBlockPos()).getBlock() != Blocks.BEDROCK) return;

                entity.setPosition(entity.getPos().add(0, -2, 0));
            }
        };
    }

    private static void buffAttribute(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, String id, float value, EntityAttributeModifier.Operation operation) {
        final var attr = entity.getAttributeInstance(attribute);
        if (attr != null) {
            final var wardenKbModifier = new EntityAttributeModifier(Identifier.of(id), value, operation);
            attr.addTemporaryModifier(wardenKbModifier);
        }
    }
}
