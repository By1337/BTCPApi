package org.by1337.tcpapi.client.event;

import org.by1337.tcpapi.api.event.Event;

public class AsyncDisconnectEvent extends Event {
    private final String reason;

    public AsyncDisconnectEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
