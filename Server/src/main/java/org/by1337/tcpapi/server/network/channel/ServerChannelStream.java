package org.by1337.tcpapi.server.network.channel;

import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.by1337.tcpapi.server.network.Client;

import java.util.Set;

public class ServerChannelStream {
    private final SpacedNameKey name;
    private final ChannelListener listener;
    private final ChannelStreamManager manager;

    ServerChannelStream(SpacedNameKey name, ChannelListener listener, ChannelStreamManager manager) {
        this.name = name;
        this.listener = listener;
        this.manager = manager;
    }

    public void close() {
        manager.closeChannelStream(name);
    }

    public boolean hasConnections() {
        return false;
    }

    public SpacedNameKey getName() {
        return name;
    }
    public void sendTo(Client client, Packet packet){
        client.sendPacket(new ChanneledPacket(packet, name));
    }

    void read(Packet packet, Client client) {
        listener.read(packet, client, this);
    }

    public Set<Client> getAllClients() {
        return manager.getAllClientByChannel(name);
    }
}
