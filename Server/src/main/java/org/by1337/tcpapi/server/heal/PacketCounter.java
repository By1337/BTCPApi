package org.by1337.tcpapi.server.heal;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PacketCounter {
    private final Counter perSecond;
    private final Counter per30Seconds;
    private final Counter perMinute;
    private final Counter per5Minutes;
    private final Counter per15Minutes;
    private long received;
    private long sent;
    private AtomicLong receivedCache = new AtomicLong();
    private AtomicLong sentCache = new AtomicLong();
    private ChannelHook channelHook;

    public PacketCounter() {
        channelHook = new ChannelHook();
        perSecond = new Counter(TimeUnit.SECONDS.toNanos(1));
        per30Seconds = new Counter(TimeUnit.SECONDS.toNanos(30));
        perMinute = new Counter(TimeUnit.MINUTES.toNanos(1));
        per5Minutes = new Counter(TimeUnit.MINUTES.toNanos(5));
        per15Minutes = new Counter(TimeUnit.MINUTES.toNanos(15));
    }

    public void tick() {
        int received = (int) receivedCache.getAndSet(0);
        int sent = (int) sentCache.getAndSet(0);
        this.received += received;
        this.sent += sent;
        long currentNanoTime = System.nanoTime();
        perSecond.tick(currentNanoTime, received, sent);
        per30Seconds.tick(currentNanoTime, received, sent);
        perMinute.tick(currentNanoTime, received, sent);
        per5Minutes.tick(currentNanoTime, received, sent);
        per15Minutes.tick(currentNanoTime, received, sent);
    }

    @Override
    public String toString() {
        return  "\t<time>: (<received>/<sent>) (<max received>/<max sent>)\n" +
                "\t1s: (" + perSecond.received + "/" + perSecond.sent + ") " + "(" + perSecond.maxReceived + "/" + perSecond.maxSent + ")\n" +
                "\t30s: (" + per30Seconds.received + "/" + per30Seconds.sent + ") " + "(" + per30Seconds.maxReceived + "/" + per30Seconds.maxSent + ")\n" +
                "\t1m: (" + perMinute.received + "/" + perMinute.sent + ") " + "(" + perMinute.maxReceived + "/" + perMinute.maxSent + ")\n" +
                "\t5m: (" + per5Minutes.received + "/" + per5Minutes.sent + ") " + "(" + per5Minutes.maxReceived + "/" + per5Minutes.maxSent + ")\n" +
                "\t15m: (" + per15Minutes.received + "/" + per15Minutes.sent + ") " + "(" + per15Minutes.maxReceived + "/" + per15Minutes.maxSent + ")\n" +
                "\tall along: received: " + received + " sent: " + sent;
    }

    public ChannelHook getChannelHook() {
        return channelHook;
    }

    @ChannelHandler.Sharable
    public class ChannelHook extends ChannelDuplexHandler{
        @Override
        public void write(ChannelHandlerContext a, Object packet, ChannelPromise c) throws Exception {
            sentCache.getAndIncrement();
            super.write(a, packet, c);
        }

        @Override
        public void channelRead(ChannelHandlerContext a, Object packet) throws Exception {
            receivedCache.getAndIncrement();
            super.channelRead(a, packet);
        }
    }


    private static class Counter {
        private long startTime = System.nanoTime();
        private final long time;
        private long received;
        private long sent;
        private long maxReceived;
        private long maxSent;

        private Counter(long time) {
            this.time = time;
        }

        private void tick(long currentNanoTime, int received, int sent) {
            long elapsedTime = currentNanoTime - startTime;
            this.received += received;
            this.sent += sent;
            if (elapsedTime >= time) {
                maxReceived = Math.max(maxReceived, this.received);
                maxSent = Math.max(maxSent, this.sent);
                this.received = 0;
                this.sent = 0;
                startTime = currentNanoTime;
            }
        }
    }
}
