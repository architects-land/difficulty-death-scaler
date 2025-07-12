package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import net.minecraft.nbt.NbtCompound;
import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyData;

public class PlayerData extends DifficultyData {
    public static final String DEATH_DAY_END_KEY = "deathDayEnd";
    public static final String BONUS_HEARTS_KEY = "bonusHearts";

    public long[] deathDayEnd = new long[]{};
    public int bonusHearts = 0;

    public static PlayerData from(NbtCompound nbt) {
        final var data = new PlayerData();
        load(nbt, data);
        data.deathDayEnd = nbt.getLongArray(DEATH_DAY_END_KEY).orElse(new long[]{});
        data.bonusHearts = nbt.getInt(BONUS_HEARTS_KEY).orElse(0);
        return data;
    }

    public NbtCompound save(NbtCompound nbt) {
        super.save(nbt);
        nbt.putLongArray(DEATH_DAY_END_KEY, deathDayEnd);
        nbt.putInt(BONUS_HEARTS_KEY, bonusHearts);
        return nbt;
    }

    public NbtCompound save() {
        return save(new NbtCompound());
    }
}
