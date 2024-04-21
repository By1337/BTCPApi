package org.by1337.tcpapi.client.network.channel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.channel.ChannelStatus;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;
import org.by1337.tcpapi.api.packet.impl.channel.RegisterChannelPacket;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.by1337.tcpapi.client.Manager;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ClientChannelStream {
    private final SpacedNameKey id;
    private final ChannelStreamPacketReader reader;
    private ChannelStatus status = ChannelStatus.CLOSED;
    private final Lock updateStatusLock = new Lock();
    private final ThreadFactory threadFactory;
    private final Queue<Packet> packets = new ConcurrentLinkedQueue<>();
    private final AtomicReference<Thread> currentPacketProcessor = new AtomicReference<>(null);
    private final ClientChannelStreamManager manager;

    ClientChannelStream(SpacedNameKey id, ChannelStreamPacketReader reader, ClientChannelStreamManager manager) {
        this.id = id;
        this.reader = reader;
        threadFactory = new ThreadFactoryBuilder().setNameFormat("ClientChannelStream id='" + id + "' #%d").build();
        this.manager = manager;
    }

    public void write(Packet packet) {
        Manager.getConnection().sendPacket(new ChanneledPacket(packet, id));
    }

    @CanIgnoreReturnValue
    public Lock register() {
        Manager.getConnection().sendPacket(new RegisterChannelPacket(id));
        return updateStatusLock;
    }

    public void unregister(){
        manager.unregister(id);
    }
    synchronized void read(Packet packet) {
        packets.offer(packet);
        if (currentPacketProcessor.get() == null) {
            var thread = threadFactory.newThread(() -> {
                Packet packet1;
                while ((packet1 = packets.poll()) != null) {
                    reader.read(packet1);
                }
                currentPacketProcessor.set(null);
            });
            currentPacketProcessor.set(thread);
            thread.start();
        }
    }

    void setStatus(ChannelStatus status) {
        this.status = status;
        updateStatusLock.update();
    }

    public ChannelStatus getStatus() {
        return status;
    }

    public void close() {

    }

    public static class Lock {
        private final Object updateStatusLock = new Object();

        public void lock() {
            lock(0L);
        }

        public void lock(long timeoutMillis) {
            try {
                synchronized (updateStatusLock) {
                    updateStatusLock.wait(timeoutMillis);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        void update() {
            synchronized (updateStatusLock) {
                updateStatusLock.notifyAll();
            }
        }
    }
}
