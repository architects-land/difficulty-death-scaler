package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyData;

public class PlayerData extends DifficultyData {
    public int deathDay = 0;
    public long[] deathDayDelay = new long[]{};
    public long bannedSince = -1;
}
