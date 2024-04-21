package org.by1337.tcpapi.server.velocity.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.server.network.Client;
import org.by1337.tcpapi.server.velocity.VelocityManager;

public class VelocityPacketReceivedEvent extends Event {
    private final VelocityManager manager;
    private final Client client;
    private final Packet packet;

    public VelocityPacketReceivedEvent(VelocityManager manager, Client client, Packet packet) {
        this.manager = manager;
        this.client = client;
        this.packet = packet;
    }

    public VelocityManager getManager() {
        return manager;
    }

    public Client getClient() {
        return client;
    }

    public Packet getPacket() {
        return packet;
    }
}
