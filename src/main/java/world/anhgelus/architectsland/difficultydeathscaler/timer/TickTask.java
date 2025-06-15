package world.anhgelus.architectsland.difficultydeathscaler.timer;

/**
 * Represents a complete task called each tick
 */
public class TickTask implements TimerAccess.TickTask {
    private boolean cancelled = false;

    public final long ticksDelay;
    public final long ticksRepeat;
    public final boolean repeating;
    public final TimerAccess.Task task;

    private long currentTicking;

    /**
     * Create a new repeating TickTask
     *
     * @param task        Task to run after the delay or the repeat time
     * @param ticksDelay  Delay before the first task's run
     * @param ticksRepeat Repeat each tick (if the repeat is 0, it will repeat each tick, if it is below 0, it will not repeat)
     * @throws IllegalArgumentException if ticksDelay is below 0
     */
    public TickTask(TimerAccess.Task task, long ticksDelay, long ticksRepeat) {
        if (ticksDelay < 0) throw new IllegalArgumentException("Ticks delay must be non-negative");
        this.ticksDelay = ticksDelay;
        this.ticksRepeat = ticksRepeat;
        this.task = task;
        repeating = ticksRepeat >= 0;
        currentTicking = ticksDelay;
    }

    /**
     * Create a new delayed TickTask
     *
     * @param task       Task to run after the delay or the repeat time
     * @param ticksDelay Delay before the first task's run
     * @throws IllegalArgumentException if ticksDelay or if ticksRepeat is below 0
     */
    public TickTask(TimerAccess.Task task, long ticksDelay) {
        if (ticksDelay < 0) throw new IllegalArgumentException("Ticks delay must be non-negative");
        this.ticksDelay = ticksDelay;
        this.ticksRepeat = -1;
        this.task = task;
        repeating = false;
        currentTicking = ticksDelay;
    }

    public void tick() {
        if (--currentTicking > 0) return;
        task.run();
        if (repeating) {
            currentTicking = ticksRepeat;
        } else {
            cancel();
        }
    }

    public long cancel() {
        if (cancelled) throw new IllegalStateException("Task already cancelled");
        cancelled = true;
        return currentTicking;
    }

    public boolean isRunning() {
        return !cancelled;
    }

    @Override
    public long getTickingBeforeRun() {
        if (cancelled) return -1;
        return currentTicking;
    }
}
