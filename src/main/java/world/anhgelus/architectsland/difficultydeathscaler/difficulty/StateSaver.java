package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaver extends PersistentState {
    public Map<UUID, PlayerData> players = new HashMap<>();

    public int deaths = 0;
    public long timeBeforeReduce = 0;
    public long timeBeforeIncrease = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        final var playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("deaths", playerData.deaths);
            playerNbt.putLong("timeBeforeReduce", playerData.timeBeforeReduce);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        nbt.putInt("deaths", deaths);
        nbt.putLong("timeBeforeReduce", timeBeforeReduce);
        nbt.putLong("timeBeforeIncrease", timeBeforeReduce);

        return nbt;
    }

    public static StateSaver createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        final var state = new StateSaver();

        final var playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.deaths = playersNbt.getCompound(key).getInt("deaths");
            playerData.timeBeforeReduce = playersNbt.getCompound(key).getLong("timeBeforeReduce");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        state.deaths = tag.getInt("deaths");
        state.timeBeforeReduce = tag.getLong("timeBeforeReduce");

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
        final var server = player.getServer();
        assert server != null;
        final var state = getServerState(server);

        return state.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }
}
