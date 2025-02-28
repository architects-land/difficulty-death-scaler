package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

public abstract class DifficultyTimer {
    protected long initialDelay = 0;
    protected long timerStart = System.currentTimeMillis() / 1000;

    protected TimerAccess timer;

    protected void delayFirstTask(long delay) {
        initialDelay = delay;
    }

    protected TickTask executeTask(TimerAccess.Task task, @Nullable TimerAccess.TickTask pastTask, long repeatEach) {
        return executeTask(task, pastTask, repeatEach, repeatEach);
    }

    protected TickTask executeTask(TimerAccess.Task task, @Nullable TimerAccess.TickTask pastTask, long delay, long repeatEach) {
        if (timer == null) throw new IllegalStateException("Timer has not been initialized");
        if (pastTask == null && initialDelay != 0) {
            TickTask tt;
            try {
                tt = new TickTask(task, (delay - initialDelay) * 20L, repeatEach * 20L);
                timer.dds_runTask(tt);
                timerStart -= initialDelay;
            } catch (IllegalArgumentException e) {
                DifficultyDeathScaler.LOGGER.error("An exception occurred while launching the first task", e);
                DifficultyDeathScaler.LOGGER.warn("Resetting delay to 0");
                initialDelay = 0;
                tt = new TickTask(task, delay * 20L, repeatEach * 20L);
                timer.dds_runTask(tt);
            }
            return tt;
        }
        final var tt = new TickTask(task, delay * 20L, repeatEach * 20L);
        timer.dds_runTask(tt);
        return tt;
    }

    protected static String formatSeconds(long time) {
        long hours = 0;
        if (time > 3600) {
            hours = Math.floorDiv(time, 3600);
        }
        long minutes = 0;
        if (hours != 0 || time > 60) {
            minutes = Math.floorDiv(time - hours * 3600, 60);
        }
        long seconds = (long) Math.floor(time - hours * 3600 - minutes * 60);

        StringBuilder sb = new StringBuilder();
        if (hours != 0) {
            sb.append(hours).append(" hours ");
        }
        if (minutes != 0 || hours != 0) {
            sb.append(minutes).append(" minutes ");
        }
        sb.append(seconds).append(" seconds");

        return sb.toString();
    }

    public long delay() {
        return delay(timerStart);
    }

    public static long delay(long timerStart) {
        return System.currentTimeMillis() / 1000 - timerStart;
    }

    protected static String formatSecondsBeforeRun(TimerAccess.TickTask task) {
        return formatSeconds(task.getTickingBeforeRun() / 20);
    }
}
