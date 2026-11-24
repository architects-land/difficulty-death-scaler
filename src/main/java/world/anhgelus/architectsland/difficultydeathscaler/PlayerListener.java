package world.anhgelus.architectsland.difficultydeathscaler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;

import java.util.Optional;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;
import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getPlayerDifficulty;

public class PlayerListener {
	public static void afterDeath(ServerPlayer player, DamageSource damageSource) {
		final var globalDifficulty = getGlobalDifficulty();
		globalDifficulty.increaseDeath();

		final var playerDifficulty = getPlayerDifficulty(player);
		playerDifficulty.increaseDeath();

		final var bounty = Bounty.get(player.getUUID());
		if (bounty == null || !(damageSource.getEntity() instanceof final ServerPlayer killer))
			return;
		bounty.onKill(getPlayerDifficulty(killer));
	}

	public static void afterRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		getGlobalDifficulty().applyModifiers(newPlayer);

		final var playerDifficulty = getPlayerDifficulty(newPlayer);
		playerDifficulty.player = Optional.of(newPlayer);
		playerDifficulty.applyModifiers();

		final var bounty = Bounty.get(newPlayer.getUUID());
		if (bounty == null) return;
		bounty.onDeath();
	}

	public static void onConnection(ServerPlayer player) {
		final var server = player.level().getServer();
		final var playerDifficulty = getPlayerDifficulty(player);
		playerDifficulty.applyModifiers();

		final var difficultyManager = getGlobalDifficulty();
		difficultyManager.applyModifiers(player);

		ModCriteria.REACH_GLOBAL_DIFFICULTY.trigger(player, difficultyManager.getNumberOfDeath());
		ModCriteria.REACH_PLAYER_DIFFICULTY.trigger(player, playerDifficulty.getNumberOfDeath());

		player.sendSystemMessage(Bounty.getBountiesMessage());

		Bounty.newBounty(server, difficultyManager, playerDifficulty);
	}

	public static void onDisconnection(ServerPlayer player) {
		final var bounty = Bounty.get(player.getUUID());
		if (bounty == null) return;
		bounty.onDisconnect();
	}

	public static InteractionResult useItemCallback(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (!(player instanceof ServerPlayer)) return InteractionResult.PASS;
		return entity instanceof LivingEntity
			? BossManager.handleBuff((ServerPlayer) player, world, hand, (LivingEntity) entity)
			: InteractionResult.PASS;
	}
}
