package org.by1337.tcpapi.server.task;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.server.ServerManager;

public abstract class ServerRunnable implements Runnable {
    private Task task;
    private final Ticker ticker;

    public ServerRunnable(Ticker ticker) {
        this.ticker = ticker;
    }

    public ServerRunnable() {
        ticker = ServerManager.getTicker();
    }

    @CanIgnoreReturnValue
    public ServerRunnable runTaskTimer(long delay) {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new Task(true, delay, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runTaskLater(long delay) {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new Task(false, delay, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runTask() {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new Task(false, 0, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runAsyncTaskTimer(long delay) {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new AsyncTask(true, delay, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runAsyncTaskLater(long delay) {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new AsyncTask(false, delay, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runAsyncTask() {
        if (task != null) {
            throw new IllegalStateException("task already started");
        }
        task = new AsyncTask(false, 0, this);
        ticker.registerTask(task);
        return this;
    }

    @CanIgnoreReturnValue
    public ServerRunnable cancel() {
        if (task == null) {
            throw new IllegalStateException("task isn't started");
        }
        task.cancel();
        return this;
    }

}
