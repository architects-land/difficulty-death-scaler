package world.anhgelus.architectsland.difficultydeathscaler.passive;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;

public class PassiveDifficulty {
    private final MinecraftServer server;

    public PassiveDifficulty(MinecraftServer server) {
        this.server = server;
    }

    public void onEntitySpawn(HostileEntity entity) {
        //TODO: handle spawn
    }
}
