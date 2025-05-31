package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(PhantomSpawner.class)
public class PhantomsSpawnerMixin {

    @Shadow
    private int cooldown;

    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    public void spawnPhantoms(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (!MobUtils.PHANTOMS_NIGHTMARE) return;
        if (!spawnMonsters) {
            cir.setReturnValue(0);
            return;
        }
        final var rand = world.random;
        cooldown--;
        if (cooldown > 0) {
            cir.setReturnValue(0);
            return;
        }
        cooldown += (15 + rand.nextInt(30)) * 20;
        // limit to 75 phantoms *per world*
        if (world.getEntitiesByType(EntityType.PHANTOM, LivingEntity::isAlive).size() > 75) {
            cir.setReturnValue(0);
            return;
        }
        var i = new AtomicInteger(0);
        world.getPlayers()
                .stream()
                .filter(p -> !p.isSpectator() && !p.isCreative())
                .filter(p -> {
                    final var pos = p.getBlockPos();
                    return pos.getY() >= world.getSeaLevel() &&
                            world.isSkyVisible(pos);
                }).forEach(p -> {
                    final var pos = p.getBlockPos();
                    final var pos2 = pos.up(20 + rand.nextInt(15))
                            .east(-10 + rand.nextInt(21))
                            .south(-10 + rand.nextInt(21));
                    final var bState = world.getBlockState(pos2);
                    final var fState = world.getFluidState(pos2);
                    if (SpawnHelper.isClearForSpawn(world, pos2, bState, fState, EntityType.PHANTOM)) {
                        var l = 1 + rand.nextInt(Getters.PLAYER_DIFFICULTY_GETTER.get(world.getServer(), p).getNumberOfDeath() + 1);
                        EntityData entityData = null;
                        for (int m = 0; m < l; ++m) {
                            final var phantom = EntityType.PHANTOM.create(world, SpawnReason.NATURAL);
                            if (phantom != null) {
                                phantom.refreshPositionAndAngles(pos2, 0.0F, 0.0F);
                                entityData = phantom.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, entityData);
                                world.spawnEntityAndPassengers(phantom);
                                i.addAndGet(1);
                            }
                        }
                    }
                });
        cir.setReturnValue(i.get());
    }
}
