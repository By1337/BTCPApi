package org.by1337.tcpapi.server.task;

import org.by1337.tcpapi.api.util.LockableList;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.util.CrashReport;
import org.by1337.tcpapi.server.heal.TPSCounter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

public class Ticker {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int TPS = 40;
    public static final int TICK_TIME_MILS = 1000 / TPS;
    private final Thread mainThread;
    private long nextTick;
    private long lastOverloadTime;
    private volatile boolean stopped;
    private final LockableList<Task> tasks = new LockableList<>();
    private final Queue<Runnable> toSync = new ConcurrentLinkedQueue<>();

    public Ticker() {
        mainThread = Thread.currentThread();
    }
    public Ticker(Thread thread) {
        mainThread = thread;
    }

    public void start() {
        if (!isMainThread()) {
            throw new IllegalStateException("is not main thread!");
        }
        nextTick = getMonotonicMillis();
        try {
            while (!stopped) {
                long i = (System.nanoTime() / (1000L * 1000L)) - nextTick;

                if (i > 5000L && nextTick - lastOverloadTime >= 30_000L) {
                    long j = i / TICK_TIME_MILS;

                    LOGGER.warning(String.format("Can't keep up! Is the server overloaded? Running %sms or %s ticks behind", i, j));

                    nextTick += j * TICK_TIME_MILS;
                    lastOverloadTime = nextTick;
                }
                nextTick += TICK_TIME_MILS;

                Runnable r;
                while ((r = toSync.poll()) != null) {
                    r.run();
                }

                tasks.lock();
                var iterator = tasks.iterator();
                while (iterator.hasNext()) {
                    var task = iterator.next();
                    task.tick();
                    if (task.isCanceled() || !task.isRepeat()) {
                        iterator.remove();
                    }
                }
                tasks.unlock();

                while (getMonotonicMillis() < nextTick) {
                  //  Thread.onSpinWait();
                    LockSupport.parkNanos(1000L);
                }
            }
        } catch (Throwable throwable) {
            new CrashReport("", throwable).saveAndPrint();
            stopped = true;
            ServerManager.getInstance().stop();
        }
    }

    public static long getMonotonicMillis() {
        return System.nanoTime() / 1_000_000L;
    }

    private void sync(Runnable runnable) {
        toSync.offer(runnable);
    }

    public void registerTask(Task task) {
        if (isMainThread()) {
            tasks.add(task);
        } else {
            sync(() -> registerTask(task));
        }
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isMainThread() {
        return mainThread == Thread.currentThread();
    }

}
