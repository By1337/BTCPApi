package org.by1337.tcpapi.server.network.channel;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.server.network.Client;

public interface ChannelListener {
    void read(Packet packet, Client client, ServerChannelStream channelStream);
}
