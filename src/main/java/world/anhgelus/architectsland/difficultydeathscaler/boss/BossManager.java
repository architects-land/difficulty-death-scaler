package world.anhgelus.architectsland.difficultydeathscaler.boss;

import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.AdvancementProvider;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BossManager {
	public static final Item BUFFING_ITEM = Items.NETHERITE_INGOT;

	private static final List<UUID> buffedBosses = new ArrayList<>();

	private static DragonBuff dragonBuff = null;

	public static InteractionResult handleBuff(ServerPlayer player, Level world, InteractionHand hand, LivingEntity e) {
		if (!(e instanceof WitherBoss ||
			e instanceof EnderDragon ||
			e instanceof ElderGuardian ||
			e instanceof Warden)) return InteractionResult.PASS;

		if (buffedBosses.contains(e.getUUID())) return InteractionResult.PASS;

		final ItemStack itemStack = player.getItemInHand(hand);
		if (!itemStack.is(BUFFING_ITEM)) return InteractionResult.PASS;
		itemStack.consume(1, player);

		ModCriteria.BOSS.trigger(player, e.getClass(), false);
		ModCriteria.ARBITRARY.trigger(player, AdvancementProvider.BUFF_BOSS);

		Boss.fromEntity(e, dragonBuff).buff();
		buffedBosses.add(e.getUUID());

		e.setHealth(e.getMaxHealth());

		final var lightingBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
		lightingBolt.setPos(e.getPos());

		world.addFreshEntity(lightingBolt);
		e.setHealth(e.getMaxHealth());

		return InteractionResult.SUCCESS;
	}

	public static void handleKill(Entity entity, DamageSource source, DifficultyManager manager) {
		if (entity instanceof EnderDragon) dragonDies();
		if (!buffedBosses.contains(entity.getUUID())) return;
		if (source.getEntity() instanceof final ServerPlayer p)
			ModCriteria.BOSS.trigger(p, entity.getClass(), true);
		buffedBosses.remove(entity.getUUID());
		manager.decreaseDeath();
		manager.updateTimerTask(); // reset timer task
	}

	public static void playerEntersEnd(ServerPlayer player, ServerLevel world) {
		if (dragonBuff == null || !dragonBuff.isDragonAlive()) {
			final var l = world.getDragons();
			if (l == null || l.isEmpty()) {
				DifficultyDeathScaler.LOGGER.info("No dragons found");
				return;
			}
			dragonBuff = new DragonBuff(l.getFirst());
		}
		DifficultyDeathScaler.LOGGER.info("Player {} enters the end", player.getName().tryCollapseToString());
		dragonBuff.playerEntersEnd(player);
	}

	public static void dragonLoaded(EnderDragon dragon, ServerLevel world) {
		if (dragonBuff != null && dragonBuff.isDragonAlive()) return;
		if (world.dimension().identifier() != Level.END.identifier()) return;
		dragonBuff = new DragonBuff(dragon);
		world.players().forEach(player -> playerEntersEnd(player, world));
	}

	public static void onDifficultyUpdate(GlobalDifficultyManager difficultyManager) {
		if (dragonBuff == null || !dragonBuff.isDragonAlive()) return;
		dragonBuff.updateDragonHealth(difficultyManager.getNumberOfDeath(), true);
	}

	private static void dragonDies() {
		dragonBuff = null;
	}
}
