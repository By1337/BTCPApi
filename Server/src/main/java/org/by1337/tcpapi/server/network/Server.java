package org.by1337.tcpapi.server.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.codec.PacketDecoder;
import org.by1337.tcpapi.api.codec.PacketEncoder;
import org.by1337.tcpapi.api.codec.Varint21FrameDecoder;
import org.by1337.tcpapi.api.codec.Varint21LengthFieldPrepender;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.event.ClientDisconnectEvent;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.logger.MarkedLogger;
import org.by1337.tcpapi.server.task.ServerRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static Logger logger = LogManager.getLogger();
    private final int port;
    private final String password;
    private volatile boolean isStopped = true;
    private EventLoopGroup loopGroup;
    private ChannelFuture channelFuture;
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    public Server(int port, String password) {
        this.port = port;
        this.password = password;
    }

    public void start() {
        start(false);
    }

    public void start(boolean debug) {
        if (!isStopped) {
            throw new IllegalStateException("server already started!");
        }
        isStopped = false;
        if (debug) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        } else {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);
        }
        Class<? extends ServerSocketChannel> channelClass;
        if (Epoll.isAvailable()) {
            channelClass = EpollServerSocketChannel.class;
            loopGroup = new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
            logger.info("Using epoll channel type");
        } else {
            channelClass = NioServerSocketChannel.class;
            loopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Server IO #%d").setDaemon(true).build());
            logger.info("Using default channel type");
        }
        channelFuture = new ServerBootstrap()
                .channel(channelClass)
                .childHandler(
                        new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel channel) {
                                try {
                                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                                } catch (ChannelException ignore) {
                                }
                                channel.pipeline()
                                        .addLast("splitter", new Varint21FrameDecoder())
                                        .addLast("decoder", new PacketDecoder(debug, new MarkedLogger("PACKET_RECEIVED", PacketDecoder.class), PacketFlow.SERVER_BOUND))
                                        .addLast("prepender", new Varint21LengthFieldPrepender())
                                        .addLast("encoder", new PacketEncoder(debug, new MarkedLogger("PACKET_SENT", PacketEncoder.class), PacketFlow.CLIENT_BOUND))
                                        .addLast("packet_counter", ServerManager.getInstance().getHealManager().getPacketCounter().getChannelHook())
                                        .addLast("auth", new UnregisteredConnection());
                            }
                        }
                )
                .group(loopGroup)
                .bind(port)
                .syncUninterruptibly()
        ;
    }

    public void shutdown() {
        if (!isStopped) {
            isStopped = true;
        } else {
            return;
        }
        logger.info("server shutdown");
        try {
            channelFuture.channel().close().sync();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "failed close channel!", e);
        } finally {
            loopGroup.shutdownGracefully();
        }
    }

    public void tick() {
        ServerManager.isMainThread();
        connections.values().forEach(Connection::tick);
    }

    public void sendAll(Packet packet) {
        connections.values().forEach(c -> c.sendPacket(packet));
    }

    public void sendAll(Packet packet, Connection except) {
        connections.values().forEach(connection -> {
            if (connection != except) {
                connection.sendPacket(packet);
            }
        });
    }

    public Collection<Connection> getAllConnections() {
        return connections.values();
    }

    public void disconnect(Connection connection, String reason) {
        if (connections.remove(connection.getId()) != null) {
            logger.info("disconnect: " + connection.getId() + " " + connection.getAddress() + " reason: " + reason);
            ServerRunnable.runTask(() -> ServerManager.getEventManager().callEvent(new ClientDisconnectEvent(connection, reason)));
        }
    }

    public void registerConnection(Connection connection) {
        if (connections.containsKey(connection.getId())) {
            throw new IllegalStateException("connection already exist");
        }
        connections.put(connection.getId(), connection);
        logger.info("new connection: " + connection.getId() + " " + connection.getAddress());
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public static Logger getLogger() {
        return logger;
    }

    public boolean isStopped() {
        return isStopped;
    }
}
