package org.by1337.tcpapi.server.console.impl;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.event.EventListener;
import org.by1337.tcpapi.api.packet.impl.PacketPingRequest;
import org.by1337.tcpapi.api.packet.impl.PacketPingResponse;
import org.by1337.tcpapi.server.Main;
import org.by1337.tcpapi.server.command.Command;
import org.by1337.tcpapi.server.event.PacketReceivedEvent;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.network.Connection;
import org.by1337.tcpapi.server.network.Server;
import org.by1337.tcpapi.server.util.CallBack;
import org.by1337.tcpapi.server.util.Pair;
import org.by1337.tcpapi.server.util.WaitNotifyCallBack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class PingCommand extends Command<Server> implements EventListener {
    private final AtomicInteger pingCounter = new AtomicInteger();
    private final Map<Integer, WaitSetValue<PacketPingResponse>> waiters = new ConcurrentHashMap<>();

    public PingCommand() {
        super("ping");
        Main.getEventManager().registerListener(this);
        executor(((sender, args) -> {
            for (Connection connection : Main.getServer().getAllConnections()) {
                LogManager.getLogger().info("pining " + connection.getId());
                ping(connection).thenAcceptAsync(pair -> {
                    var con = pair.getLeft();
                    var response = pair.getRight();
                    if (response == null) {
                        LogManager.getLogger().info("ping lost: " + con.getId());
                    } else {
                        LogManager.getLogger().info("ping " + response.getPing() + " " + con.getId());
                    }
                });
            }
        }));
    }

    private CompletableFuture<Pair<Connection, @Nullable PacketPingResponse>> ping(Connection connection) {
        return CompletableFuture.supplyAsync(() -> {
            int id = pingCounter.getAndIncrement();
            try {
                PacketPingRequest pingRequest = new PacketPingRequest(System.currentTimeMillis(), id);
                var waiter = new WaitSetValue<PacketPingResponse>();
                waiters.put(id, waiter);
                connection.sendPacket(pingRequest);
                waiter.wait_(10_000);
                return Pair.of(connection, waiter.getValue());
            } catch (InterruptedException e) {
                LogManager.getLogger().log(Level.SEVERE, "", e);
                return Pair.of(connection, null);
            } finally {
                waiters.remove(id);
            }
        });
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketReceivedEvent packetReceived
                && packetReceived.getPacket() instanceof PacketPingResponse pingResponse) {
            var waiter = waiters.remove(pingResponse.getId());
            if (waiter != null) {
                waiter.back(pingResponse);
            }
        }
    }

    private static class WaitSetValue<T> implements CallBack<T> {
        private T value;

        @Override
        public void back(@Nullable T value) {
            synchronized (this) {
                this.value = value;
                notifyAll();
            }
        }

        public void wait_(long ms) throws InterruptedException {
            synchronized (this) {
                wait(ms);
            }
        }

        public void wait_() throws InterruptedException {
            wait_(0L);
        }

        public T getValue() {
            return value;
        }
    }
}
