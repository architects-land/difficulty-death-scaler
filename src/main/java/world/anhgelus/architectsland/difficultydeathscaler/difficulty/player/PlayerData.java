package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.nbt.NbtCompound;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyData;

public class PlayerData extends DifficultyData {
    public static final String DEATH_DAY_KEY = "deathDay";
    public static final String DEATH_DAY_DELAY_KEY = "deathDayDelay";
    public static final String BANNED_SINCE_KEY = "bannedSince";

    public int deathDay = 0;
    public long[] deathDayDelay = new long[]{};
    public long bannedSince = -1;

    public static PlayerData from(NbtCompound nbt) {
        final var data = new PlayerData();
        load(nbt, data);
        data.deathDay = nbt.getInt(DEATH_DAY_KEY, 0);
        data.deathDayDelay = nbt.getLongArray(DEATH_DAY_DELAY_KEY).orElse(new long[]{});
        if (nbt.contains(BANNED_SINCE_KEY)) data.bannedSince = nbt.getLong(BANNED_SINCE_KEY, -1);
        return data;
    }

    public NbtCompound save(NbtCompound nbt) {
        super.save(nbt);
        nbt.putInt(DEATH_DAY_KEY, deathDay);
        nbt.putLongArray(DEATH_DAY_DELAY_KEY, deathDayDelay);
        nbt.putLong(BANNED_SINCE_KEY, bannedSince);
        return nbt;
    }

    public NbtCompound save() {
        return save(new NbtCompound());
    }
}
