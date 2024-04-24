package org.by1337.tcp.velocityclient;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.*;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.by1337.tcp.velocity.api.VelocityPacketRegistry;
import org.by1337.tcp.velocity.api.packet.PlayerConnectToServerPacket;
import org.by1337.tcp.velocity.api.packet.PlayerDisconnectPacket;
import org.by1337.tcp.velocityclient.network.Connection;
import org.by1337.tcp.velocityclient.util.Config;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "velocityclient",
        name = "VelocityClient",
        version = "1.0"
)
public class VelocityClient {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Config cfg;
    private Connection connection;
    private boolean isStopped;
    private Runnable initConnection;
    private boolean firstConnect = true;

    @Inject
    public VelocityClient(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        VelocityPacketRegistry.load();
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        cfg = new Config(dataDirectory.toFile());
        initConnection = () -> {
            if (isStopped) return;
            server.getScheduler().buildTask(VelocityClient.this, () -> {
                connection = new Connection(cfg, logger, initConnection, VelocityClient.this);
                connection.start(true);
            }).delay(firstConnect ? 0 : 5, TimeUnit.SECONDS).schedule();
            firstConnect = false;
        };
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        initConnection.run();
    }

    @Subscribe
    public void on(ProxyShutdownEvent event) {
        isStopped = true;
        connection.shutdown();
    }

    @Subscribe
    public void on(ServerConnectedEvent event) {
        String server = cfg.getAssociations().getOrDefault(event.getServer().getServerInfo().getName(), event.getServer().getServerInfo().getName());
        String previousServer = event.getPreviousServer().map(registeredServer -> registeredServer.getServerInfo().getName()).orElse(null);
        if (connection != null && connection.isOpen()) {
            connection.sendPacket(new PlayerConnectToServerPacket(event.getPlayer().getUniqueId(), server,
                    previousServer == null ? null : cfg.getAssociations().getOrDefault(previousServer, previousServer)
            ));
        }
    }

    @Subscribe
    public void on(DisconnectEvent event) {
        var server = event.getPlayer().getCurrentServer().orElse(null);
        if (connection != null && connection.isOpen()) {
            connection.sendPacket(new PlayerDisconnectPacket(
                    event.getPlayer().getUniqueId(),
                    server == null ? null : cfg.getAssociations().getOrDefault(server.getServerInfo().getName(), server.getServerInfo().getName())
            ));
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Config getCfg() {
        return cfg;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isStopped() {
        return isStopped;
    }
}
