package org.by1337.tcpapi.server.task;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.server.ServerManager;

import java.util.concurrent.TimeUnit;

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
    public ServerRunnable runTaskTimer(long time, TimeUnit timeUnit) {
        return runTaskTimer(timeUnit.toMillis(time) / (1000 / Ticker.TPS));
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
    public ServerRunnable runTaskLater(long time, TimeUnit timeUnit) {
        return runTaskLater(timeUnit.toMillis(time) / (1000 / Ticker.TPS));
    }

    @CanIgnoreReturnValue
    public static Task runTaskLater(long delay, Runnable runnable) {
        Task task  = new Task(false, delay, runnable);
        ServerManager.getTicker().registerTask(task);
        return task;
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
    public static Task runTask(Runnable runnable) {
        var task = new Task(false, 0, runnable);
        ServerManager.getTicker().registerTask(task);
        return task;
    }

    @CanIgnoreReturnValue
    public ServerRunnable runAsyncTaskTimer(long time, TimeUnit timeUnit) {
        return runAsyncTaskTimer(timeUnit.toMillis(time) / (1000 / Ticker.TPS));
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
    public ServerRunnable runAsyncTaskLater(long time, TimeUnit timeUnit) {
        return runAsyncTaskLater(timeUnit.toMillis(time) / (1000 / Ticker.TPS));
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
