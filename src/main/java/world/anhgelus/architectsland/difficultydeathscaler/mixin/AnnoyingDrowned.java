package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;
import world.anhgelus.architectsland.difficultydeathscaler.utils.MobUtils;

@Mixin(Drowned.class)
public abstract class AnnoyingDrowned extends Monster {
	protected AnnoyingDrowned(EntityType<? extends Monster> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(at = @At("RETURN"), method = "<init>")
	protected void init(EntityType<? extends Zombie> type, Level level, CallbackInfo ci) {
		if (Getters.GLOBAL_DIFFICULTY_GETTER == null) return;
		final var difficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get();
		MobUtils.customSpawn(getRandom(), difficulty.getNumberOfDeath() - 9, 30, () -> {
			// drowned with more tridents
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
			return null;
		});
	}
}
