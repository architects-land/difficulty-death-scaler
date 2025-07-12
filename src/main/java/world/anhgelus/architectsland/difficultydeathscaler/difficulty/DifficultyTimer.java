package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import org.jetbrains.annotations.Nullable;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TickTask;
import world.anhgelus.architectsland.difficultydeathscaler.timer.TimerAccess;

public abstract class DifficultyTimer {
    protected TimerAccess timer;
    private long initialDelay = 0;

    private static long[] getFormatInfo(long time) {
        long hours = 0;
        if (time > 3600) hours = Math.floorDiv(time, 3600);

        long minutes = 0;
        if (hours != 0 || time > 60) minutes = Math.floorDiv(time - hours * 3600, 60);

        final long seconds = (long) Math.floor(time - hours * 3600 - minutes * 60);

        return new long[]{hours, minutes, seconds};
    }

    protected static String formatSeconds(long time) {
        final var info = getFormatInfo(time);
        final var hours = info[0];
        final var minutes = info[1];
        final var seconds = info[2];

        StringBuilder sb = new StringBuilder();
        if (hours != 0) sb.append(hours).append(" hours ");
        if (minutes != 0 || hours != 0) {
            sb.append(minutes).append(" minutes");
            if (hours != 0 && minutes != 0) return sb.toString(); // avoid sending seconds
            sb.append(" ");
        }
        sb.append(seconds).append(" seconds");

        return sb.toString();
    }

    protected static String formatSecondsBig(long time) {
        final var info = getFormatInfo(time);
        final var hours = info[0];
        final var minutes = info[1];
        final var seconds = info[2];

        if (hours != 0) return String.format("%d hours", hours);
        if (minutes != 0) {
            return String.format("%d minutes", minutes);
        }
        return String.format("%d seconds", seconds);
    }

    protected static String formatSecondsBeforeRun(TimerAccess.TickTask task) {
        return formatSeconds(task.getTickingBeforeRun() / 20);
    }

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
                tt = new TickTask(task, delay * 20L - initialDelay, repeatEach * 20L);
                timer.dds_runTask(tt);
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
}
