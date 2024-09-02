package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.Timer;
import java.util.TimerTask;

public class DifficultyTimer {
    protected long initialDelay = 0;
    protected long timerStart = System.currentTimeMillis() / 1000;

    protected Timer timer;

    protected void delayFirstTask(long delay) {
        initialDelay = delay;
    }

    protected void executeTask(TimerTask task, @Nullable TimerTask pastTask, long repeatEach) {
        executeTask(task, pastTask, repeatEach, repeatEach);
    }

    protected void executeTask(TimerTask task, @Nullable TimerTask pastTask, long delay, long repeatEach) {
        if (pastTask == null && initialDelay != 0) {
            try {
                timer.schedule(task, (delay - initialDelay) * 1000L, repeatEach * 1000L);
                timerStart -= initialDelay;
            } catch (IllegalArgumentException e) {
                DifficultyDeathScaler.LOGGER.error("An exception occurred while launching first task", e);
                DifficultyDeathScaler.LOGGER.warn("Resetting delay to 0");
                initialDelay = 0;
                timer.schedule(task, delay * 1000L, repeatEach * 1000L);
            }
            return;
        }
        timer.schedule(task, delay * 1000L, repeatEach * 1000L);
    }

    public long delay() {
        return System.currentTimeMillis() / 1000 - timerStart;
    }
}
