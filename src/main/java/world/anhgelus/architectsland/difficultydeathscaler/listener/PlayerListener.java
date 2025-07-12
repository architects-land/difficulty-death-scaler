package world.anhgelus.architectsland.difficultydeathscaler.listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.ModCriteria;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

public class PlayerListener {
    public static void afterDeath(ServerPlayerEntity player, DamageSource damageSource) {
        final var globalDifficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get();
        globalDifficulty.increaseDeath();
        globalDifficulty.save();

        final var playerDifficulty = Getters.PLAYER_DIFFICULTY_GETTER.get(player.getServer(), player);
        playerDifficulty.increaseDeath();
        playerDifficulty.save();

        final var bounty = Bounty.get(player.getUuid());
        if (bounty == null || !(damageSource.getAttacker() instanceof final ServerPlayerEntity killer)) return;
        bounty.onKill(Getters.PLAYER_DIFFICULTY_GETTER.get(killer.getServer(), killer));
    }

    public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        Getters.GLOBAL_DIFFICULTY_GETTER.get().applyModifiers(newPlayer);

        final var playerDifficulty = Getters.PLAYER_DIFFICULTY_GETTER.get(newPlayer.getServer(), newPlayer);
        playerDifficulty.player = newPlayer;
        playerDifficulty.applyModifiers();

        final var bounty = Bounty.get(newPlayer.getUuid());
        if (bounty == null) return;
        bounty.onDeath();
    }

    public static void onConnection(ServerPlayerEntity player) {
        final var server = player.getServer();
        final var playerDifficulty = Getters.PLAYER_DIFFICULTY_GETTER.get(server, player);
        playerDifficulty.applyModifiers();

        final var difficultyManager = Getters.GLOBAL_DIFFICULTY_GETTER.get();
        difficultyManager.applyModifiers(player);

        for (int i = 0; i <= difficultyManager.getNumberOfDeath(); i++)
            ModCriteria.REACH_DIFFICULTY.trigger(player, i);

        player.sendMessage(Bounty.getBountiesMessage());

        Bounty.newBounty(server, difficultyManager, playerDifficulty);
    }

    public static void onDisconnection(ServerPlayerEntity player) {
        final var bounty = Bounty.get(player.getUuid());
        if (bounty == null) return;
        bounty.onDisconnect();
    }

    public static ActionResult useItemCallback(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        return entity instanceof LivingEntity
                ? BossManager.handleBuff(player, world, hand, (LivingEntity) entity)
                : ActionResult.PASS;
    }
}
