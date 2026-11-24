package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalDifficultyManager;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;

@Mixin(Zombie.class)
public abstract class AnnoyingZombie extends Monster {
	protected AnnoyingZombie(EntityType<? extends Monster> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(
		at = @At("RETURN"),
		method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V"
	)
	protected void init(EntityType<? extends Zombie> type, Level level, CallbackInfo ci) {
		final var difficulty = getGlobalDifficulty();
		if (difficulty == null) return;
		MobUtils.customSpawn(getRandom(), difficulty.getNumberOfDeath() - 9, 30, () -> {
			// turn into baby zombies
			this.setBaby(true);
			return null;
		});
	}

	@Inject(at = @At("HEAD"), method = "registerGoals")
	protected void newGoals(CallbackInfo ci) {
		if (!GlobalDifficultyManager.areZombiesBetter()) return;
		goalSelector.addGoal(3, new OpenDoorGoal(this, false));
	}

	@Inject(at = @At("RETURN"), method = "registerGoals")
	protected void betterGoals(CallbackInfo ci) {
		if ((Zombie) (Object) this instanceof ZombifiedPiglin) return;
		if (GlobalDifficultyManager.areZombiesBetter())
			MobUtils.commonBetterGoals(this, targetSelector);
	}
}
