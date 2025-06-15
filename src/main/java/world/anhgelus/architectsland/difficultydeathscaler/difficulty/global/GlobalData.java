package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import net.minecraft.nbt.NbtCompound;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyData;

public class GlobalData extends DifficultyData {
    public static final String TIME_BEFORE_INCREASE_KEY = "timeBeforeIncrease";
    public static final String INCREASE_ENABLED_KEY = "increaseEnabled";

    public long timeBeforeIncrease = 0;
    public boolean increaseEnabled = false;

    public static GlobalData from(NbtCompound nbt) {
        final var data = new GlobalData();
        load(nbt, data);
        data.timeBeforeIncrease = nbt.getLong(TIME_BEFORE_INCREASE_KEY, DifficultyIncrease.SECONDS_BEFORE_INCREASE);
        data.increaseEnabled = nbt.getBoolean(INCREASE_ENABLED_KEY, false);
        return data;
    }

    public NbtCompound save(NbtCompound nbt) {
        super.save(nbt);
        nbt.putLong(TIME_BEFORE_INCREASE_KEY, timeBeforeIncrease);
        nbt.putBoolean(INCREASE_ENABLED_KEY, increaseEnabled);
        return nbt;
    }

    public NbtCompound save() {
        return save(new NbtCompound());
    }
}
