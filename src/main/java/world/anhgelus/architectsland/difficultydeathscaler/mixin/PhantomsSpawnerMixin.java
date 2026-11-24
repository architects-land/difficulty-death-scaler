package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getPlayerDifficulty;

@Mixin(PhantomSpawner.class)
public class PhantomsSpawnerMixin {

	@Shadow
	private int nextTick;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void spawnPhantoms(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
		if (!MobUtils.PHANTOMS_NIGHTMARE) return;
		ci.cancel();
		if (!spawnEnemies) return;
		final var rand = level.getRandom();
		nextTick--;
		if (nextTick > 0) return;
		nextTick += (15 + rand.nextInt(30)) * 20;
		// limit to 75 phantoms *per world*
		if (level.getEntities(EntityTypes.PHANTOM, LivingEntity::isAlive).size() > 75) return;
		var i = new AtomicInteger(0);
		level.players()
			.stream()
			.filter(p -> !p.isSpectator() && !p.isCreative())
			.filter(p -> {
				final var pos = p.blockPosition();
				return pos.getY() >= level.getSeaLevel() &&
					level.canSeeSky(pos);
			}).forEach(p -> {
				final var pos = p.blockPosition();
				final var pos2 = pos.above(20 + rand.nextInt(15))
					.east(-10 + rand.nextInt(21))
					.south(-10 + rand.nextInt(21));
				final var bState = level.getBlockState(pos2);
				final var fState = level.getFluidState(pos2);
				if (NaturalSpawner.isValidEmptySpawnBlock(level, pos2, bState, fState, EntityTypes.PHANTOM)) {
					var l = 1 + rand.nextInt(getPlayerDifficulty(p).getNumberOfDeath() + 1);
					SpawnGroupData entityData = null;
					for (int m = 0; m < l; ++m) {
						final var phantom = EntityTypes.PHANTOM.create(level, EntitySpawnReason.NATURAL);
						if (phantom != null) {
							phantom.snapTo(pos2, 0.0F, 0.0F);
							entityData = phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, entityData);
							level.addFreshEntityWithPassengers(phantom);
							i.addAndGet(1);
						}
					}
				}
			});
	}
}
