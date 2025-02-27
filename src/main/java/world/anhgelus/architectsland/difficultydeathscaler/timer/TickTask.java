package world.anhgelus.architectsland.difficultydeathscaler.timer;

/**
 * Represents a complete task called each tick
 */
public class TickTask implements TimerAccess.TickTask {
    private boolean cancelled = false;

    public final long ticksDelay;
    public final long ticksRepeat;
    public final boolean repeat;
    public final TimerAccess.Task task;

    private long currentTicking;

    /**
     * Create a new repeating TickTask
     *
     * @param task        Task to run after the delay or the repeat time
     * @param ticksDelay  Delay before the first task's run
     * @param ticksRepeat Repeat each tick
     */
    public TickTask(TimerAccess.Task task, long ticksDelay, long ticksRepeat) {
        this.ticksDelay = ticksDelay;
        this.ticksRepeat = ticksRepeat;
        this.task = task;
        repeat = true;
        currentTicking = ticksDelay;
    }

    /**
     * Create a new TickTask
     *
     * @param task       Task to run after the delay or the repeat time
     * @param ticksDelay Delay before the first task's run
     */
    public TickTask(TimerAccess.Task task, long ticksDelay) {
        this.ticksDelay = ticksDelay;
        this.ticksRepeat = -1;
        this.task = task;
        repeat = false;
        currentTicking = ticksDelay;
    }

    /**
     * Tick the task
     */
    public void tick() {
        if (--currentTicking != 0) return;
        task.run();
        if (repeat) {
            currentTicking = ticksRepeat;
        } else {
            cancel();
        }
    }

    /**
     * Cancel the task
     *
     * @return the remaining ticks before the run of the Task
     * @throws IllegalStateException if the task is already cancelled
     */
    public long cancel() {
        if (cancelled) throw new IllegalStateException("Task already cancelled");
        cancelled = true;
        return currentTicking;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
