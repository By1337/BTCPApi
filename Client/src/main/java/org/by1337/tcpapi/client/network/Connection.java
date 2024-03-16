package org.by1337.tcpapi.client.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.TimeoutException;
import org.by1337.tcpapi.api.codec.PacketDecoder;
import org.by1337.tcpapi.api.codec.PacketEncoder;
import org.by1337.tcpapi.api.codec.Varint21FrameDecoder;
import org.by1337.tcpapi.api.codec.Varint21LengthFieldPrepender;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.DisconnectPacket;
import org.by1337.tcpapi.api.packet.impl.PacketPingRequest;
import org.by1337.tcpapi.api.packet.impl.PacketPingResponse;
import org.by1337.tcpapi.client.Manager;
import org.by1337.tcpapi.client.event.AsyncDisconnectEvent;
import org.by1337.tcpapi.client.event.AsyncPacketReceivedEvent;
import org.by1337.tcpapi.client.event.PacketReceivedEvent;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection extends SimpleChannelInboundHandler<Packet> {
    private final String ip;
    private final int port;
    private final String id;
    private final String password;
    private ChannelFuture channelFuture;
    private EventLoopGroup loopGroup;
    private final Logger logger;
    private volatile boolean stopped = true;
    private boolean disconnect;
    private Channel channel;
    private SocketAddress address;
    private long connected;
    private final Object authWait = new Object();
    private boolean authorized;
    private final Queue<Packet> packets = new ConcurrentLinkedQueue<>();

    public Connection(String ip, int port, String id, String password, Logger logger) {
        this.ip = ip;
        this.port = port;
        this.id = id;
        this.password = password;
        this.logger = logger;
    }

    void connect(Channel channel, SocketAddress address) {
        connected = System.currentTimeMillis();
        this.channel = channel;
        this.address = address;
    }

    void authorized() {
        authorized = true;
        synchronized (authWait) {
            authWait.notifyAll();
        }
    }

    public void authWait() {
        authWait(0L);
    }

    public void authWait(long l) {
        try {
            synchronized (authWait) {
                authWait.wait(l);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        start(false);
    }

    public void start(boolean debug) {
        if (!stopped) {
            throw new IllegalStateException("client already started");
        }
        stopped = false;
        Class<? extends SocketChannel> channelClass;
        if (Epoll.isAvailable()) {
            channelClass = EpollSocketChannel.class;
            loopGroup = new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
            logger.info("Using epoll channel type");
        } else {
            channelClass = NioSocketChannel.class;
            loopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build());
            logger.info("Using default channel type");
        }
        try {
            channelFuture = new Bootstrap()
                    .channel(channelClass)
                    .handler(
                            new ChannelInitializer<>() {
                                @Override
                                protected void initChannel(Channel channel) {
                                    try {
                                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                                    } catch (ChannelException ignore) {
                                    }
                                    channel.pipeline()
                                            .addLast("splitter", new Varint21FrameDecoder())
                                            .addLast("decoder", new PacketDecoder(debug, logger))
                                            .addLast("prepender", new Varint21LengthFieldPrepender())
                                            .addLast("encoder", new PacketEncoder(debug, logger))
                                            .addLast("auth", new ConnectionAuth(Connection.this));
                                }
                            }
                    )
                    .group(loopGroup)
                    .connect(ip, port).syncUninterruptibly()
                    .channel().closeFuture();

            stopped = false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed connect", e);
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        if (!stopped) {
            stopped = true;
        } else {
            return;
        }
        logger.info("client shutdown");
        try {
            channelFuture.channel().close().sync();
            channelFuture = null;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "failed close channel", e);
        } finally {
            loopGroup.shutdownGracefully();
            loopGroup = null;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (packet instanceof DisconnectPacket disconnectPacket) {
            disconnect(disconnectPacket.getReason());
        } else if (packet instanceof PacketPingRequest packetPingRequest) {
            sendPacket(new PacketPingResponse(
                    (int) (System.currentTimeMillis() - packetPingRequest.getTime()),
                    packetPingRequest.getId()
            ));
        } else {
            var event = new AsyncPacketReceivedEvent(this, packet);
            Manager.getEventManager().callEvent(event);
            if (!event.isCanceled()) {
                packets.offer(packet);
            }
        }
    }

    public void tick() {
        Manager.isMainThread();
        Packet packet;
        while ((packet = packets.poll()) != null) {
            Manager.getEventManager().callEvent(new PacketReceivedEvent(this, packet));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.disconnect("End of stream");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof TimeoutException) {
            this.disconnect("Timed out");
        } else {
            this.disconnect("Internal Exception: " + cause);
            logger.log(Level.SEVERE, "", cause);
        }
    }

    public void disconnect(String reason) {
        if (!disconnect) {
            this.channel.close().awaitUninterruptibly();
            disconnect = true;
            logger.severe("disconnected reason: " + reason);
            Manager.getEventManager().callEvent(new AsyncDisconnectEvent(reason));
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getConnected() {
        return connected;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void sendPacket(Packet packet) {
        if (channel.isOpen()) {
            channel.writeAndFlush(packet);
        }
    }
}
