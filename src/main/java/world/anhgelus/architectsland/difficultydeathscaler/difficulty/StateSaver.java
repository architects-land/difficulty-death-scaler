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

        final var playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            final var playerData = new PlayerData();
            final var compound = playersNbt.getCompound(key);

            playerData.deaths = compound.getInt("deaths");
            playerData.timeBeforeReduce = compound.getLong("timeBeforeReduce");
            playerData.deathDay = compound.getInt("deathDay");
            playerData.deathDayDelay = compound.getLongArray("deathDayDelay");
            playerData.totalOfDeath = compound.getInt("totalOfDeath");
            if (compound.contains("bannedSince")) playerData.bannedSince = compound.getLong("bannedSince");
            if (compound.contains("secondsLowerDifficulty"))
                playerData.secondsLowerDifficulty = compound.getLong("secondsLowerDifficulty");

            state.players.put(UUID.fromString(key), playerData);
        });
        state.difficulty.deaths = tag.getInt("deaths");
        state.difficulty.timeBeforeReduce = tag.getLong("timeBeforeReduce");
        state.difficulty.timeBeforeIncrease = tag.getLong("timeBeforeIncrease");
        state.difficulty.increaseEnabled = tag.getBoolean("increaseEnabled");
        state.difficulty.totalOfDeath = tag.getInt("totalOfDeath");
        state.difficulty.secondsLowerDifficulty = tag.getInt("secondsLowerDifficulty");

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
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("deaths", playerData.deaths);
            playerNbt.putLong("timeBeforeReduce", playerData.timeBeforeReduce);
            playerNbt.putInt("deathDay", playerData.deathDay);
            playerNbt.putLongArray("deathDayDelay", playerData.deathDayDelay);
            playerNbt.putInt("totalOfDeath", playerData.totalOfDeath);
            playerNbt.putLong("bannedSince", playerData.bannedSince);
            playerNbt.putLong("secondsLowerDifficulty", playerData.secondsLowerDifficulty);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        nbt.putInt("deaths", difficulty.deaths);
        nbt.putLong("timeBeforeReduce", difficulty.timeBeforeReduce);
        nbt.putLong("timeBeforeIncrease", difficulty.timeBeforeIncrease);
        nbt.putBoolean("increaseEnabled", difficulty.increaseEnabled);
        nbt.putInt("totalOfDeath", difficulty.totalOfDeath);
        nbt.putLong("secondsLowerDifficulty", difficulty.secondsLowerDifficulty);

        return nbt;
    }
}
