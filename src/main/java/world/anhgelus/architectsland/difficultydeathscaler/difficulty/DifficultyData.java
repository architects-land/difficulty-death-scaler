package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import net.minecraft.nbt.NbtCompound;

public class DifficultyData {
    public static final String DEATHS_KEY = "deaths";
    public static final String TIME_BEFORE_REDUCE_KEY = "timeBeforeReduce";
    public static final String TOTAL_OF_DEATH_KEY = "totalOfDeath";
    public static final String SECONDS_LOWER_DIFFICULTY_KEY = "secondsLowerDifficulty";

    public int deaths = 0;
    public long timeBeforeReduce = 0;
    public int totalOfDeath = 0;
    public long secondsLowerDifficulty = 0;

    protected static void load(NbtCompound nbt, DifficultyData data) {
        data.deaths = nbt.getInt(DEATHS_KEY, 0);
        data.timeBeforeReduce = nbt.getLong(TIME_BEFORE_REDUCE_KEY, 0);
        data.totalOfDeath = nbt.getInt(TOTAL_OF_DEATH_KEY, 0);
        data.secondsLowerDifficulty = nbt.getLong(SECONDS_LOWER_DIFFICULTY_KEY, 0);
    }

    protected NbtCompound save(NbtCompound nbt) {
        nbt.putInt(DEATHS_KEY, deaths);
        nbt.putLong(TIME_BEFORE_REDUCE_KEY, timeBeforeReduce);
        nbt.putInt(TOTAL_OF_DEATH_KEY, totalOfDeath);
        nbt.putLong(SECONDS_LOWER_DIFFICULTY_KEY, secondsLowerDifficulty);
        return nbt;
    }

    protected NbtCompound save() {
        return save(new NbtCompound());
    }
}
