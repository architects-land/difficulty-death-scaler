package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalData;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaver extends PersistentState {
    public static final String PLAYERS_KEY = "players";
    private static final Type<StateSaver> type = new Type<>(
            StateSaver::new,
            StateSaver::createFromNbt,
            null
    );
    public Map<UUID, PlayerData> players = new HashMap<>();
    public GlobalData difficulty;

    public static StateSaver createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        final var state = new StateSaver();
        state.difficulty = new GlobalData();
        final var playersNbt = tag.getCompound(PLAYERS_KEY);
        playersNbt.getKeys().forEach(key -> {
            final var compound = playersNbt.getCompound(key);
            state.players.put(UUID.fromString(key), PlayerData.from(compound));
        });
        state.difficulty = GlobalData.from(tag);

        return state;
    }

    public static StateSaver getServerState(MinecraftServer server) {
        final var world = server.getOverworld();
        final var persistentStateManager = world.getPersistentStateManager();

        final var state = persistentStateManager.getOrCreate(type, DifficultyDeathScaler.MOD_ID);

        state.markDirty();

        if (state.difficulty == null) state.difficulty = new GlobalData();

        return state;
    }

    public static PlayerData getPlayerState(ServerPlayerEntity player) {
        return getPlayerState(player.server, player.getUuid());
    }

    public static PlayerData getPlayerState(MinecraftServer server, UUID uuid) {
        final var state = getServerState(server);
        return state.players.computeIfAbsent(uuid, u -> new PlayerData());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        final var playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> playersNbt.put(uuid.toString(), playerData.save()));
        nbt.put(PLAYERS_KEY, playersNbt);
        return difficulty.save(nbt);
    }
}
