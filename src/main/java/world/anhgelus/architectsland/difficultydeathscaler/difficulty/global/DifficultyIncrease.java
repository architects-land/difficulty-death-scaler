package world.anhgelus.architectsland.difficultydeathscaler.difficulty.global;

import world.anhgelus.architectsland.difficultydeathscaler.difficulty.DifficultyTimer;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

public class DifficultyIncrease extends DifficultyTimer {
    public static final int SECONDS_BEFORE_INCREASE = 3 * 24 * 60 * 60; // 3 days
    public static final int SECONDS_EACH_INCREASE = 12 * 60 * 60; // 12 hours

    private final GlobalDifficultyManager manager;

    private TickTask increaseTask;
    private boolean enabled;

    public DifficultyIncrease(GlobalDifficultyManager manager, TimerAccess timer, long initialDelay, boolean enabled) {
        this.manager = manager;
        this.enabled = enabled;
        this.timer = timer;
        delayFirstTask(initialDelay);
        restart();
    }

    public void restart() {
        if (increaseTask != null && increaseTask.isRunning()) increaseTask.cancel();
        final TimerAccess.Task task = () -> {
            enabled = true;
            manager.increaseDeath(true);
            manager.stopAutomaticDecrease();
        };
        if (enabled) increaseTask = executeTask(task, increaseTask, SECONDS_EACH_INCREASE);
        else increaseTask = executeTask(task, increaseTask, SECONDS_BEFORE_INCREASE, SECONDS_EACH_INCREASE);
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getTickingBeforeRun() {
        return increaseTask.getTickingBeforeRun();
    }
}
