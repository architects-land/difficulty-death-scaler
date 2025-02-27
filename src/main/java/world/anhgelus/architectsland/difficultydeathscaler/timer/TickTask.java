package world.anhgelus.architectsland.difficultydeathscaler.timer;

/**
 * Represents a complete task called each tick
 */
public class TickTask {
    /**
     * Represents a task to run after ticking
     */
    @FunctionalInterface
    public interface Task {
        void run();
    }

    private boolean cancelled = false;

    private final long ticksDelay;
    private final long ticksRepeat;
    private final boolean repeat;
    private final Task task;

    private long currentTicking;

    /**
     * Create a new repeating TickTask
     *
     * @param ticksDelay  Delay before the first task's run
     * @param ticksRepeat Repeat each tick
     * @param task        Task to run after the delay or the repeat time
     */
    public TickTask(long ticksDelay, long ticksRepeat, Task task) {
        this.ticksDelay = ticksDelay;
        this.ticksRepeat = ticksRepeat;
        this.task = task;
        repeat = true;
        currentTicking = ticksDelay;
    }

    /**
     * Create a new TickTask
     *
     * @param ticksDelay Delay before the first task's run
     * @param task       Task to run after the delay or the repeat time
     */
    public TickTask(long ticksDelay, Task task) {
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
     */
    public long cancel() {
        cancelled = true;
        return currentTicking;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getTicksDelay() {
        return ticksDelay;
    }

    public long getTicksRepeat() {
        return ticksRepeat;
    }
}
