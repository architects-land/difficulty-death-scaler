package world.anhgelus.architectsland.difficultydeathscaler;

import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class PlayerDeathManager {
    public final long deathDecreaseCooldown;

    private final Map<PlayerEntity, Integer> recentPlayerDeaths = new HashMap<>();

    public PlayerDeathManager() {
        deathDecreaseCooldown = 5 * 60;
        setupTimer();
    }

    public PlayerDeathManager(long deathDecreaseCooldown) {
        this.deathDecreaseCooldown = deathDecreaseCooldown;
        setupTimer();
    }

    public void handleDeath(PlayerEntity player) {
        int playerDeaths = recentPlayerDeaths.getOrDefault(player, 0) + 1;

        recentPlayerDeaths.put(player, playerDeaths);
        updatePlayerPenalization(player, playerDeaths);
    }

    public int getPlayerDeaths(PlayerEntity player) {
        return recentPlayerDeaths.getOrDefault(player, 0);
    }

    private void setupTimer() {
        var timer = new Timer();
        var timerTask = new TimerTask() {
            @Override
            public void run() {
                decreasePlayerDeaths();
            }
        };
        timer.schedule(timerTask, deathDecreaseCooldown * 1000, deathDecreaseCooldown * 1000);
    }

    private void decreasePlayerDeaths() {
        for (Map.Entry<PlayerEntity, Integer> entry : recentPlayerDeaths.entrySet()) {
            int playerDeaths = Math.max(entry.getValue() - 1, 0);
            entry.setValue(playerDeaths);
            updatePlayerPenalization(entry.getKey(), playerDeaths);
        }
    }

    private static void updatePlayerPenalization(PlayerEntity player, int deaths) {
        // TODO: penalize player
    }
}
