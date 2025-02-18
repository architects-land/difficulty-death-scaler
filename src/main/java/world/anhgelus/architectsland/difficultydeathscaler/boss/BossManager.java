package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BossManager {
    public static final Item BUFFING_ITEM = Items.NETHERITE_INGOT;

    private static final List<UUID> buffedBosses = new ArrayList<>();

    private static DragonBuff dragonBuff = null;

    public static ActionResult handleBuff(PlayerEntity player, World world, Hand hand, LivingEntity e) {
        if (!(e instanceof WitherEntity ||
                e instanceof EnderDragonEntity ||
                e instanceof ElderGuardianEntity ||
                e instanceof WardenEntity)) return ActionResult.PASS;

        if (buffedBosses.contains(e.getUuid())) return ActionResult.PASS;

        final ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(BUFFING_ITEM)) return ActionResult.PASS;
        itemStack.decrementUnlessCreative(1, player);

        Boss.fromEntity(e, dragonBuff).buff();
        buffedBosses.add(e.getUuid());

        e.setHealth(e.getMaxHealth());

        final var lightingBolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightingBolt.setPosition(e.getPos());

        world.spawnEntity(lightingBolt);
        e.setHealth(e.getMaxHealth());

        return ActionResult.SUCCESS;
    }

    public static void handleKill(Entity entity, DifficultyManager manager) {
        if (entity instanceof EnderDragonEntity) dragonDies();
        if (!buffedBosses.contains(entity.getUuid())) return;
        buffedBosses.remove(entity.getUuid());
        manager.decreaseDeath();
    }

    public static void playerEntersEnd(ServerPlayerEntity player) {
        if (dragonBuff == null) {
            final var l = player.getServerWorld().getEntitiesByType(
                    TypeFilter.instanceOf(EnderDragonEntity.class),
                    (p) -> !p.isDead()
            );
            if (l == null || l.isEmpty()) return; // no dragon or dragon is dead
            final var dragon = l.getFirst();
            dragonBuff = new DragonBuff(dragon);
        }
        dragonBuff.playerEntersEnd(player);
    }

    private static void dragonDies() {
        dragonBuff = null;
    }
}
