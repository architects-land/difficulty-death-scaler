package world.anhgelus.architectsland.difficultydeathscaler.listener;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.boss.BossManager;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.Bounty;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import java.util.Map;
import java.util.UUID;

public class PlayerListener {
    public static void onKill(ServerPlayerEntity player, DamageSource damageSource) {
        Getters.GLOBAL_DIFFICULTY_GETTER.get().increaseDeath();

        final var bounty = Getters.BOUNTY_GETTER.get(player.getUuid());
        if (bounty == null || !(damageSource.getAttacker() instanceof final ServerPlayerEntity killer)) return;
        bounty.onKill(Getters.PLAYER_DIFFICULTY_GETTER.get(killer.server, killer));
    }

    public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        Getters.GLOBAL_DIFFICULTY_GETTER.get().applyModifiers(newPlayer);

        final var playerDifficulty = Getters.PLAYER_DIFFICULTY_GETTER.get(newPlayer.server, newPlayer);
        playerDifficulty.player = newPlayer;
        playerDifficulty.increaseDeath();
        playerDifficulty.applyModifiers();

        final var bounty = Getters.BOUNTY_GETTER.get(newPlayer.getUuid());
        if (bounty == null) return;
        bounty.onDeath();
    }

    public static void onConnection(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server, Map<UUID, Bounty> bountyMap) {
        final var playerDifficulty = Getters.PLAYER_DIFFICULTY_GETTER.get(server, handler.player);
        playerDifficulty.applyModifiers();

        final var difficultyManager = Getters.GLOBAL_DIFFICULTY_GETTER.get();

        difficultyManager.applyModifiers(handler.player);

        final var bounty = Bounty.newBounty(server, difficultyManager, playerDifficulty);
        if (bounty != null) bountyMap.put(handler.player.getUuid(), bounty);
    }

    public static void onDisconnection(ServerPlayNetworkHandler handler, MinecraftServer server) {
        final var bounty = Getters.BOUNTY_GETTER.get(handler.player.getUuid());
        if (bounty == null) return;
        bounty.onDisconnect();
    }

    public static ActionResult useItemCallback(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (!(entity instanceof LivingEntity)) {
            return ActionResult.PASS;
        }
        return BossManager.handleBuff(player, world, hand, (LivingEntity) entity);
    }
}
