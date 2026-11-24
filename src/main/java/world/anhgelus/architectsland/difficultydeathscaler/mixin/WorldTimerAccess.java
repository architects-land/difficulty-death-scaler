package world.anhgelus.architectsland.difficultydeathscaler.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class WorldTimerAccess implements TimerAccess {
	@Unique
	private final List<TimerAccess.TickTask> tasks = new ArrayList<>();

	@Unique
	private final List<TimerAccess.TickTask> tasksToAdd = new ArrayList<>();

	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(BooleanSupplier haveTime, CallbackInfo ci) {
		if (!haveTime.getAsBoolean()) return;
		tasks.stream().filter(TickTask::isRunning).forEach(TickTask::tick);
		tasks.addAll(tasksToAdd);
		tasksToAdd.clear();
	}

	@Override
	public void dds_runTask(TimerAccess.TickTask task) {
		tasksToAdd.add(task);
	}

	@Override
	public void dds_cancel() {
		tasks.stream().filter(TickTask::isRunning).forEach(TickTask::cancel);
	}

	@Override
	public List<TickTask> dds_getTasks() {
		return tasks.stream().filter(TickTask::isRunning).toList();
	}
}
