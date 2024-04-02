package org.by1337.tcpapi.server.task;

import org.by1337.tcpapi.server.logger.LogManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class AsyncTask extends Task {
    private AtomicBoolean isDone = new AtomicBoolean(true);

    public AsyncTask(boolean repeat, long delay, Runnable runnable) {
        super(repeat, delay, runnable);
    }

    public AsyncTask(long delay, Runnable runnable) {
        super(delay, runnable);
    }

    public AsyncTask(Runnable runnable) {
        super(runnable);
    }

    @Override
    public void tick() {
        if (isDone.get()) {
            isDone.set(false);
            CompletableFuture.runAsync(super::tick).thenAcceptAsync(v -> {
                synchronized (AsyncTask.this) {
                    isDone.set(true);
                }
            });
        }
    }

    @Override
    public boolean isCanceled() {
        synchronized (this) {
            return super.isCanceled();
        }
    }

    @Override
    public void cancel() {
        synchronized (this) {
            super.cancel();
        }
    }
}
