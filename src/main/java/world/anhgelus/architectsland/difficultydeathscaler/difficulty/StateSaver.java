package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.global.GlobalData;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.player.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaver extends PersistentState {
    public static final String PLAYERS_KEY = "players";
    public static final String GLOBAL_KEY = "global";
    public static final Codec<StateSaver> CODEC = RecordCodecBuilder.create(i -> i.group(
            NbtCompound.CODEC.fieldOf(GLOBAL_KEY).forGetter(s -> s.difficulty.save()),
            Codec.unboundedMap(Codec.STRING, NbtCompound.CODEC).fieldOf(PLAYERS_KEY).forGetter(StateSaver::getPlayers)
    ).apply(i, StateSaver::new));
    private static final PersistentStateType<StateSaver> type = new PersistentStateType<>(
            DifficultyDeathScaler.MOD_ID,
            StateSaver::new,
            CODEC,
            null
    );
    public Map<UUID, PlayerData> players;
    public GlobalData difficulty;

    public StateSaver(NbtCompound difficulty, Map<String, NbtCompound> players) {
        this.difficulty = GlobalData.from(difficulty);
        final var np = new HashMap<UUID, PlayerData>();
        players.forEach((s, playerData) -> {
            np.put(UUID.fromString(s), PlayerData.from(playerData));
        });
        this.players = np;
    }

    public StateSaver() {
        this(new NbtCompound(), new HashMap<>());
    }

    public static StateSaver getServerState(MinecraftServer server) {
        final var world = server.getOverworld();
        final var persistentStateManager = world.getPersistentStateManager();

        final var state = persistentStateManager.getOrCreate(type);

        state.markDirty();

        if (state.difficulty == null) state.difficulty = new GlobalData();

        return state;
    }

    public static PlayerData getPlayerState(ServerPlayerEntity player) {
        return getPlayerState(player.getServer(), player.getUuid());
    }

    public static PlayerData getPlayerState(MinecraftServer server, UUID uuid) {
        final var state = getServerState(server);
        return state.players.computeIfAbsent(uuid, u -> new PlayerData());
    }

    public Map<String, NbtCompound> getPlayers() {
        final var np = new HashMap<String, NbtCompound>();
        players.forEach((uuid, playerData) -> {
            np.put(uuid.toString(), playerData.save());
        });
        return np;
    }
}
