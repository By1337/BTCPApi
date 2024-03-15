
package org.by1337.tcpapi.client.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.client.network.Connection;

/**
 * Called immediately after receiving a packet.
 * Any blocking operations are prohibited.
 * If the event is not canceled, this packet will be received synchronously in the {@link PacketReceivedEvent} event on the next tick.
 */
public class AsyncPacketReceivedEvent extends Event {
    private final Connection connection;
    private final Packet packet;
    private boolean canceled;

    public AsyncPacketReceivedEvent(Connection connection, Packet packet) {
        this.connection = connection;
        this.packet = packet;
    }

    /**
     * @return the client that received the packet
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the received packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * @return whether this event is canceled
     */
    public boolean isCanceled() {
        return canceled;
    }


    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
