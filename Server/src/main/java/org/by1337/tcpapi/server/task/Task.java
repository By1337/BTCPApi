package org.by1337.tcpapi.server.task;

import org.by1337.tcpapi.server.logger.LogManager;

import java.util.logging.Level;

public class Task {
    private final boolean repeat;
    private final long delay;
    private final Runnable runnable;
    private long currentTick;
    private boolean canceled;

    public Task(boolean repeat, long delay, Runnable runnable) {
        this.repeat = repeat;
        this.delay = delay;
        this.runnable = runnable;
    }

    public Task(long delay, Runnable runnable) {
        this.repeat = false;
        this.delay = delay;
        this.runnable = runnable;
    }

    public Task(Runnable runnable) {
        this.repeat = false;
        this.delay = 0;
        this.runnable = runnable;
    }

    public void tick() {
        if (canceled) return;
        if (delay == 0 || currentTick % delay == 0) {
            try {
                runnable.run();
            } catch (Throwable t) {
                LogManager.getLogger().log(Level.SEVERE, "An error occurred while executing the task: " + this, t);
            }
            currentTick = 0;
        }
        currentTick++;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public long getDelay() {
        return delay;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        this.canceled = true;
    }
}
