package world.anhgelus.architectsland.difficultydeathscaler.timer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface TimerAccess {
    /**
     * Run a task (called each tick ticked)
     *
     * @param task Task to run
     */
    void dds_runTask(TickTask task);

    void dds_cancel();

    /**
     * Get the timer linked to the overworld
     *
     * @param server Current server
     * @return TimerAccess linked to the overworld
     */
    static TimerAccess getTimerFromOverworld(MinecraftServer server) {
        final var timer = (TimerAccess) server.getWorld(World.OVERWORLD);
        if (timer == null)
            throw new NullPointerException("Impossible to get TimerAccess from the overworld (it is null)");
        return timer;
    }
}
