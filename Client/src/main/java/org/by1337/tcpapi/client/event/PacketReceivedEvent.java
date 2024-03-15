package org.by1337.tcpapi.client.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.client.network.Connection;

public class PacketReceivedEvent extends Event {
    private final Connection connection;
    private final Packet packet;

    public PacketReceivedEvent(Connection connection, Packet packet) {
        this.connection = connection;
        this.packet = packet;
    }

    public Connection getConnection() {
        return connection;
    }

    public Packet getPacket() {
        return packet;
    }
}
