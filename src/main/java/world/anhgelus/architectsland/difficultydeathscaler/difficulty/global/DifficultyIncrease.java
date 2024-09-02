package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;

import java.util.Timer;
import java.util.TimerTask;

public class DifficultyIncrease extends DifficultyTimer {
    public static final int SECONDS_BEFORE_INCREASE = 3*24*60*60; // 3 days
    public static final int SECONDS_EACH_INCREASE = 24*60*60; // 24 hours

    private TimerTask increaseTask;

    public DifficultyIncrease(Timer timer, long initialDelay) {
        this.timer = timer;
        delayFirstTask(initialDelay);
    }

    public void restart(GlobalDifficultyManager manager) {
        if (increaseTask != null) increaseTask.cancel();
        final var task = new TimerTask() {
            @Override
            public void run() {
                manager.increaseDeath(true);
                manager.stopAutomaticDecrease();
            }
        };
        executeTask(task, increaseTask, SECONDS_BEFORE_INCREASE, SECONDS_EACH_INCREASE);
        increaseTask = task;
        timerStart = System.currentTimeMillis() / 1000;
    }
}
