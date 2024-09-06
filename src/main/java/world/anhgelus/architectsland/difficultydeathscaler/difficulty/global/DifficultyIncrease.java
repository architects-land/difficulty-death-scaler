package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;

import java.util.Timer;
import java.util.TimerTask;

public class DifficultyIncrease extends DifficultyTimer {
    public static final int SECONDS_BEFORE_INCREASE = 3*24*60*60; // 3 days
    public static final int SECONDS_EACH_INCREASE = 12*60*60; // 12 hours

    private final GlobalDifficultyManager manager;

    private TimerTask increaseTask;
    private boolean enabled;

    public DifficultyIncrease(GlobalDifficultyManager manager, Timer timer, long initialDelay, boolean enabled) {
        this.manager = manager;
        this.enabled = enabled;
        this.timer = timer;
        delayFirstTask(initialDelay);
        restart();
    }

    public void restart() {
        if (increaseTask != null) increaseTask.cancel();
        final var task = new TimerTask() {
            @Override
            public void run() {
                enabled = true;
                manager.increaseDeath(true);
                manager.stopAutomaticDecrease();
            }
        };
        if (enabled) executeTask(task, increaseTask, SECONDS_EACH_INCREASE);
        else executeTask(task, increaseTask, SECONDS_BEFORE_INCREASE, SECONDS_EACH_INCREASE);
        enabled = false;
        increaseTask = task;
        timerStart = System.currentTimeMillis() / 1000;
    }

    public boolean enabled() {
        return enabled;
    }
}
