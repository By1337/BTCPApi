package org.by1337.tcpapi.client.network.channel;

import org.by1337.tcpapi.api.packet.Packet;

public interface ChannelStreamPacketReader {
    void read(Packet packet);
}
