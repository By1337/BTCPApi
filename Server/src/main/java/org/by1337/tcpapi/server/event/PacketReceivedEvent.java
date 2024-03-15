package org.by1337.tcpapi.server.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.server.network.Client;

public class PacketReceivedEvent extends Event {
    private final Client client;
    private final Packet packet;

    public PacketReceivedEvent(Client client, Packet packet) {
        this.client = client;
        this.packet = packet;
    }

    public Client getClient() {
        return client;
    }

    public Packet getPacket() {
        return packet;
    }
}
