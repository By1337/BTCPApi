package org.by1337.tcpapi.server.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.server.network.Client;

public class ClientDisconnectEvent extends Event {
    private final Client client;
    private final String reason;
    public ClientDisconnectEvent(Client client, String reason) {
        this.client = client;
        this.reason = reason;
    }

    public Client getClient() {
        return client;
    }

    public String getReason() {
        return reason;
    }
}
