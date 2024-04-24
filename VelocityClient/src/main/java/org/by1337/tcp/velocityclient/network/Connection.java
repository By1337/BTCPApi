package org.by1337.tcp.velocityclient.network;

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
import org.by1337.tcp.velocity.api.packet.FindPlayerRequestPacket;
import org.by1337.tcp.velocity.api.packet.FindPlayerResponsePacket;
import org.by1337.tcp.velocityclient.VelocityClient;
import org.by1337.tcp.velocityclient.util.Config;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.codec.PacketDecoder;
import org.by1337.tcpapi.api.codec.PacketEncoder;
import org.by1337.tcpapi.api.codec.Varint21FrameDecoder;
import org.by1337.tcpapi.api.codec.Varint21LengthFieldPrepender;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.DisconnectPacket;
import org.by1337.tcpapi.api.packet.impl.PacketPingRequest;
import org.by1337.tcpapi.api.packet.impl.PacketPingResponse;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;
import org.by1337.tcpapi.api.packet.impl.channel.RegisterChannelPacket;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class Connection extends SimpleChannelInboundHandler<Packet> {
    private final Config cfg;
    private final Logger logger;
    private volatile boolean stopped = true;
    private EventLoopGroup loopGroup;
    private ChannelFuture channelFuture;
    private boolean disconnect;
    private Channel channel;
    private long connected;
    private SocketAddress address;
    private final Runnable onDisconnect;
    private final SpacedNameKey CHANNEL_ID = new SpacedNameKey("velocity", "main_channel");
    private final VelocityClient plugin;

    public Connection(Config cfg, Logger logger, Runnable onDisconnect, VelocityClient plugin) {
        this.cfg = cfg;
        this.plugin = plugin;
        this.logger = logger;
        this.onDisconnect = onDisconnect;
    }

    void connect(Channel channel, SocketAddress address) {
        connected = System.currentTimeMillis();
        this.channel = channel;
        this.address = address;
        channel.writeAndFlush(new RegisterChannelPacket(CHANNEL_ID));
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
                                            .addLast("decoder", new PacketDecoder(debug, java.util.logging.Logger.getLogger("decoder"), PacketFlow.CLIENT_BOUND))
                                            .addLast("prepender", new Varint21LengthFieldPrepender())
                                            .addLast("encoder", new PacketEncoder(debug, java.util.logging.Logger.getLogger("encoder"), PacketFlow.SERVER_BOUND))
                                            .addLast("auth", new ConnectionAuth(Connection.this));
                                }
                            }
                    )
                    .group(loopGroup)
                    .connect(cfg.getIp(), cfg.getPort()).syncUninterruptibly()
                    .channel().closeFuture();

            stopped = false;

        } catch (Exception e) {
            logger.error("failed connect", e);
            shutdown();
            throw e;
        }
    }

    public Config getCfg() {
        return cfg;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (packet instanceof DisconnectPacket disconnectPacket) {
            disconnect(disconnectPacket.getReason());
        } else if (packet instanceof PacketPingRequest packetPingRequest) {
            channel.writeAndFlush(new PacketPingResponse(
                    (int) (System.currentTimeMillis() - packetPingRequest.getTime()),
                    packetPingRequest.getId()
            ));
        } else if (packet instanceof ChanneledPacket channeledPacket) {
            if (channeledPacket.getChannelID().equals(CHANNEL_ID)) {
                if (channeledPacket.getPacket() instanceof FindPlayerRequestPacket findPlayerRequest) {
                    var opt = plugin.getServer().getPlayer(findPlayerRequest.getPlayer());
                    if (opt.isPresent()) {
                        var player = opt.get();
                        var server = player.getCurrentServer().orElse(null);
                        sendPacket(new FindPlayerResponsePacket(player.getUniqueId(),
                                server == null ? null : cfg.getAssociations().getOrDefault(server.getServerInfo().getName(), server.getServerInfo().getName())
                        ));
                    } else {
                        sendPacket(new FindPlayerResponsePacket(findPlayerRequest.getPlayer(), null));
                    }
                }
            }
        }
    }

    public void disconnect(String reason) {
        if (!disconnect) {
            try {
                this.channel.close().awaitUninterruptibly();
                if (channelFuture != null) {
                    channelFuture.channel().close().awaitUninterruptibly();
                    channelFuture = null;
                }
                if (loopGroup != null) {
                    loopGroup.shutdownGracefully();
                    loopGroup = null;
                }
                disconnect = true;
                logger.error("disconnected reason: " + reason);
            } finally {
                onDisconnect.run();
            }
        }
    }

    public void shutdown() {
        if (!stopped) {
            stopped = true;
            disconnect = true;
        } else {
            return;
        }
        logger.info("client shutdown");
        try {
            if (channelFuture != null) {
                channelFuture.channel().close().sync();
                channelFuture = null;
            }
        } catch (InterruptedException e) {
            logger.error("failed close channel", e);
        } finally {
            if (loopGroup != null) {
                loopGroup.shutdownGracefully();
                loopGroup = null;
            }
            onDisconnect.run();
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
            logger.error("", cause);
        }
    }

    public void sendPacket(Packet packet) {
        if (channel.isOpen()) {
            channel.writeAndFlush(new ChanneledPacket(packet, CHANNEL_ID));
        }
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isStopped() {
        return stopped;
    }

    public EventLoopGroup getLoopGroup() {
        return loopGroup;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getConnected() {
        return connected;
    }

    public SocketAddress getAddress() {
        return address;
    }
}
