package org.by1337.tcpapi.server.network;

import org.by1337.tcpapi.api.packet.Packet;

import java.net.SocketAddress;

public interface Client {
    SocketAddress getAddress();
    String getId();
    Server getServer();
    long getConnected();
    boolean isDisconnected();
    void sendPacket(Packet packet);
    void disconnect(String reason);
}
