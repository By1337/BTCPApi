package org.by1337.tcpapi.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.PacketPingRequest;
import org.by1337.tcpapi.api.packet.impl.PacketPingResponse;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.event.PacketReceivedEvent;
import org.by1337.tcpapi.server.logger.MarkedLogger;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection extends SimpleChannelInboundHandler<Packet> implements Client {
    private static final Logger LOGGER = new MarkedLogger("CLIENT", Connection.class);
    private final Channel channel;
    private final SocketAddress address;
    private final String id;
    private final Server server;
    private final long connected;
    private boolean disconnected;
    private final Queue<Packet> packets = new ConcurrentLinkedQueue<>();

    public Connection(Channel channel, SocketAddress address, String id, Server server) {
        connected = System.currentTimeMillis();
        this.channel = channel;
        this.address = address;
        this.id = id;
        this.server = server;
        if (!channel.isOpen()) {
            throw new IllegalArgumentException("channel is closed!");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PacketPingRequest packetPingRequest) {
            sendPacket(new PacketPingResponse(
                    (int) (System.currentTimeMillis() - packetPingRequest.getTime()),
                    packetPingRequest.getId()
            ));
        } else {
            packets.offer(packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.disconnect("End of stream");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        disconnect("connection unregister");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof TimeoutException) {
            this.disconnect("Timed out");
        } else {
            this.disconnect("Internal Exception: " + cause);
            LOGGER.log(Level.SEVERE, "", cause);
        }
    }

    public void tick() {
        ServerManager.isMainThread();
        Packet packet;
        while ((packet = packets.poll()) != null) {
            ServerManager.getEventManager().callEvent(new PacketReceivedEvent(this, packet));
        }
    }

    @Override
    public void disconnect(String reason) {
        if (!disconnected) {
            this.channel.close().awaitUninterruptibly();
            server.disconnect(this, reason);
            disconnected = true;
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        if (channel.isOpen()) {
            channel.writeAndFlush(packet);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return connected == that.connected && disconnected == that.disconnected && Objects.equals(address, that.address) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, id, connected, disconnected);
    }

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getId() {
        return id;
    }

    public Server getServer() {
        return server;
    }

    public long getConnected() {
        return connected;
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}
