package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerData;

import java.util.*;

public class StateSaver extends PersistentState {
    public Map<UUID, PlayerData> players = new HashMap<>();

    public int deaths = 0;
    public long timeBeforeReduce = 0;
    public long timeBeforeIncrease = 0;
    public boolean increaseEnabled = false;
    public int totalOfDeath = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        final var playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("deaths", playerData.deaths);
            playerNbt.putLong("timeBeforeReduce", playerData.timeBeforeReduce);
            playerNbt.putInt("deathDay", playerData.deathDay);
            playerNbt.putLongArray("deathDayDelay", playerData.deathDayDelay);
            playerNbt.putInt("totalOfDeath", playerData.totalOfDeath);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        nbt.putInt("deaths", deaths);
        nbt.putLong("timeBeforeReduce", timeBeforeReduce);
        nbt.putLong("timeBeforeIncrease", timeBeforeIncrease);
        nbt.putBoolean("increaseEnabled", increaseEnabled);
        nbt.putInt("totalOfDeath", totalOfDeath);

        return nbt;
    }

    public static StateSaver createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        final var state = new StateSaver();

        final var playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            final var playerData = new PlayerData();
            final var compound = playersNbt.getCompound(key);

            playerData.deaths = compound.getInt("deaths");
            playerData.timeBeforeReduce = compound.getLong("timeBeforeReduce");
            playerData.deathDay = compound.getInt("deathDay");
            playerData.deathDayDelay = compound.getLongArray("deathDayDelay");
            playerData.totalOfDeath = compound.getInt("totalOfDeath");

            state.players.put(UUID.fromString(key), playerData);
        });
        state.deaths = tag.getInt("deaths");
        state.timeBeforeReduce = tag.getLong("timeBeforeReduce");
        state.timeBeforeIncrease = tag.getLong("timeBeforeIncrease");
        state.increaseEnabled = tag.getBoolean("increaseEnabled");
        state.totalOfDeath = tag.getInt("totalOfDeath");

        return state;
    }

    private static final Type<StateSaver> type = new Type<>(
            StateSaver::new,
            StateSaver::createFromNbt,
            null
    );

    public static StateSaver getServerState(MinecraftServer server) {
        final var world = server.getWorld(World.OVERWORLD);
        assert world != null;
        final var persistentStateManager = world.getPersistentStateManager();

        final var state = persistentStateManager.getOrCreate(type, DifficultyDeathScaler.MOD_ID);

        state.markDirty();

        return state;
    }

    public static PlayerData getPlayerState(ServerPlayerEntity player) {
        return getPlayerState(player.server, player.getUuid());
    }

    public static PlayerData getPlayerState(MinecraftServer server, UUID uuid) {
        final var state = getServerState(server);
        return state.players.computeIfAbsent(uuid, u -> new PlayerData());
    }
}
