package org.by1337.tcpapi.server.task;

import junit.framework.TestCase;
import org.junit.Test;

public class TickerTest {
    Ticker ticker;

    public TickerTest() throws InterruptedException {
        var t = new Thread(() -> ticker.start());
        ticker = new Ticker(t);
        t.start();
        Thread.sleep(10);
    }

    @Test
    public void taskTest() throws Exception {
        var r = new ServerRunnable(ticker) {
            int x;

            @Override
            public void run() {
                x++;
                if (x != 1) {
                    throw new IllegalStateException();
                }
            }
        }.runTask();
        Thread.sleep(50);
        r.cancel();
    }

    @Test
    public void taskTest2() throws Exception {
        var r = new ServerRunnable(ticker) {
            int x;

            @Override
            public void run() {
                x++;
                if (x == 2) {
                    cancel();
                }
                if (x == 3) {
                    throw new IllegalStateException();
                }
            }
        }.runTaskTimer(0);
        Thread.sleep(50);
        r.cancel();
    }

    @Test
    public void taskTest3() throws Exception {
        var r = new ServerRunnable(ticker) {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runAsyncTask();
        var r1 = new ServerRunnable(ticker) {
            int x;

            @Override
            public void run() {
                x++;
                if (x == 2) {
                    cancel();
                }
                if (x == 3) {
                    throw new IllegalStateException();
                }
            }
        }.runTaskTimer(0);
        Thread.sleep(50);
        r1.cancel();
    }

}