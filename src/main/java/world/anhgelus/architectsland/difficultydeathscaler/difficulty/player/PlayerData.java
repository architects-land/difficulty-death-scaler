package world.anhgelus.architectsland.difficultydeathscaler.difficulty.player;

public class PlayerData {
    public int deaths = 0;
    public long timeBeforeReduce = 0;
    public int deathDay = 0;
    public int totalOfDeath = 0;
    public long[] deathDayDelay = new long[]{};
    public long bannedSince = -1;
    public long secondsLowerDifficulty = 0;
}
