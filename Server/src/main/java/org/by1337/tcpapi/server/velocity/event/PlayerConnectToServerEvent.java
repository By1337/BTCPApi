package org.by1337.tcpapi.server.velocity.event;

import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.server.network.Client;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerConnectToServerEvent extends Event {
    private final UUID player;
    private final @Nullable Client server;
    private final @Nullable Client previousServer;

    public PlayerConnectToServerEvent(UUID player, @Nullable Client server, @Nullable Client previousServer) {
        this.player = player;
        this.server = server;
        this.previousServer = previousServer;
    }

    public UUID getPlayer() {
        return player;
    }

    public @Nullable Client getServer() {
        return server;
    }

    public @Nullable Client getPreviousServer() {
        return previousServer;
    }

    @Override
    public String toString() {
        return "PlayerConnectToServerEvent{" +
                "player=" + player +
                ", server=" + server +
                ", previousServer=" + previousServer +
                '}';
    }
}
