package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BossManager {
    public static final Item buffingItem = Items.NETHERITE_INGOT;

    private static final List<UUID> buffedBosses = new ArrayList<>();

    public static ActionResult handleBuff(PlayerEntity player, Hand hand, Entity entity) {
        if (!(entity instanceof WitherEntity ||
                entity instanceof EnderDragonEntity ||
                entity instanceof ElderGuardianEntity ||
                entity instanceof WardenEntity)) return ActionResult.PASS;

        if (buffedBosses.contains(entity.getUuid())) return ActionResult.PASS;

        final ItemStack itemStack = player.getStackInHand(hand);
        // TODO: check that boss is not buffed
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

        return ActionResult.SUCCESS;
    }

    public static void handleKill(Entity entity, DifficultyManager manager) {
        if (!buffedBosses.contains(entity.getUuid())) {
            return;
        }
        buffedBosses.remove(entity.getUuid());
        manager.decreaseDeath(entity.getServer());
    }

    private static BuffableBoss<ElderGuardianEntity> getBoss(ElderGuardianEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {

            }
        };
    }

    private static BuffableBoss<EnderDragonEntity> getBoss(EnderDragonEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {

            }
        };
    }

    private static BuffableBoss<WardenEntity> getBoss(WardenEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {

            }
        };
    }

    private static BuffableBoss<WitherEntity> getBoss(WitherEntity entity) {
        return new BuffableBoss<>(entity) {
            @Override
            public void buff() {

            }
        };
    }
}
