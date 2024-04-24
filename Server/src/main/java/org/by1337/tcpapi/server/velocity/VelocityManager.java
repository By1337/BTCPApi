package org.by1337.tcpapi.server.velocity;

import org.by1337.tcp.velocity.api.packet.PlayerConnectToServerPacket;
import org.by1337.tcp.velocity.api.packet.PlayerDisconnectPacket;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.network.Client;
import org.by1337.tcpapi.server.network.Server;
import org.by1337.tcpapi.server.network.channel.ChannelListener;
import org.by1337.tcpapi.server.network.channel.ChannelStreamManager;
import org.by1337.tcpapi.server.network.channel.ServerChannelStream;
import org.by1337.tcpapi.server.velocity.event.PlayerConnectToServerEvent;
import org.by1337.tcpapi.server.velocity.event.PlayerDisconnectEvent;
import org.by1337.tcpapi.server.velocity.event.VelocityPacketReceivedEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VelocityManager implements ChannelListener {
    private final SpacedNameKey CHANNEL_ID = new SpacedNameKey("velocity", "main_channel");
    private final Server server;
    private final ServerChannelStream stream;
    private final HashMap<UUID, Client> playerMap = new HashMap<>();

    public VelocityManager(ChannelStreamManager channelStreamManager, Server server) {
        this.server = server;
        stream = channelStreamManager.registerChannelStream(CHANNEL_ID, this);
    }

    @Override
    public void read(Packet packet, Client client, ServerChannelStream channelStream) {
        if (packet instanceof PlayerConnectToServerPacket playerConnectToServer) {
            System.out.println(playerConnectToServer);
            Client s = server.getConnectionById(playerConnectToServer.getServer());
            Client s1 = server.getConnectionById(playerConnectToServer.getPreviousServer().orElse(null));
            PlayerConnectToServerEvent event = new PlayerConnectToServerEvent(playerConnectToServer.getPlayer(), s, s1);
            on(event);
            ServerManager.getEventManager().callEvent(event);
        } else if (packet instanceof PlayerDisconnectPacket disconnectPacket) {
            System.out.println(disconnectPacket);
            Client server = this.server.getConnectionById(disconnectPacket.getFromServer().orElse(null));
            var event = new PlayerDisconnectEvent(disconnectPacket.getPlayer(), server);
            on(event);
            ServerManager.getEventManager().callEvent(event);
        } else {
            VelocityPacketReceivedEvent event = new VelocityPacketReceivedEvent(this, client, packet);
            ServerManager.getEventManager().callEvent(event);
        }
    }

    private void on(PlayerConnectToServerEvent event) {
        playerMap.put(event.getPlayer(), event.getServer());
        System.out.println("event.getServer()=" + event.getServer());
    }

    private void on(PlayerDisconnectEvent event) {
        playerMap.remove(event.getPlayer());
        System.out.println("event.getPlayer()=" + event.getPlayer());
    }

    public void sendPacket(Packet packet) {
        if (!isAvailable()) throw new IllegalStateException("velocity is not connected!");
        stream.sendTo(stream.getAllClients().stream().findFirst().get(), packet);
    }

    public boolean isAvailable() {
        return !stream.getAllClients().isEmpty();
    }

    public @Nullable Client findPlayer(UUID player) {
        return playerMap.get(player);
    }
}
