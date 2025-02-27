package world.anhgelus.architectsland.difficultydeathscaler.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface TimerAccess {
    @FunctionalInterface
    interface End {
        void end();
    }

    void dds_setTimer(long ticksUntilEnd, End end);

    static TimerAccess getTimerFromOverworld(MinecraftServer server) {
        final var timer = (TimerAccess) server.getWorld(World.OVERWORLD);
        if (timer == null)
            throw new NullPointerException("Impossible to get TimerAccess from the overworld (it is null)");
        return timer;
    }
}
