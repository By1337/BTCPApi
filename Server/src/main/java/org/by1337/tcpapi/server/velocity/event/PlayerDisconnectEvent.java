package org.by1337.tcpapi.server.velocity.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.server.network.Client;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerDisconnectEvent extends Event {
    private final UUID player;
    private final @Nullable Client fromServer;

    public PlayerDisconnectEvent(UUID player, @Nullable Client fromServer) {
        this.player = player;
        this.fromServer = fromServer;
    }

    public UUID getPlayer() {
        return player;
    }

    public Client getFromServer() {
        return fromServer;
    }
}
