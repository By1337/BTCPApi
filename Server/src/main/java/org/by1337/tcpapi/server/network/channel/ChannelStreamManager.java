package org.by1337.tcpapi.server.network.channel;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.event.EventListener;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.channel.*;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.event.ClientDisconnectEvent;
import org.by1337.tcpapi.server.event.PacketReceivedEvent;
import org.by1337.tcpapi.server.logger.MarkedLogger;
import org.by1337.tcpapi.server.network.Client;

import java.security.PublicKey;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ChannelStreamManager implements EventListener {
    private static final Logger LOGGER = new MarkedLogger("ChannelStreamManager", ChannelStreamManager.class);
    private Map<SpacedNameKey, Set<Client>> serverWait = new HashMap<>();
    private Map<SpacedNameKey, ServerChannelStream> channelMap = new HashMap<>();
    private Map<SpacedNameKey, Set<Client>> clientMap = new HashMap<>();

    public ServerChannelStream registerChannelStream(SpacedNameKey name, ChannelListener listener) {
        ServerManager.isMainThread();
        if (channelMap.containsKey(name)) {
            throw new IllegalStateException("ServerChannelStream with name \"" + name + "\" already exists!");
        }
        ServerChannelStream stream = new ServerChannelStream(name, listener, this);
        channelMap.put(name, stream);
        var set = serverWait.remove(name);
        if (set != null) {
            for (Client client : set) {
                client.sendPacket(new ChannelStatusPacket(name, ChannelStatus.OPENED));
                clientMap.computeIfAbsent(name, k -> new HashSet<>()).add(client);
            }
        }
        return stream;
    }

    @CanIgnoreReturnValue
    public ServerChannelStream closeChannelStream(SpacedNameKey nameKey) {
        ServerChannelStream stream = channelMap.remove(nameKey);
        if (stream == null) {
            throw new IllegalStateException("channel is not exist!");
        }
        var set = clientMap.remove(nameKey);
        if (set != null) {
            for (Client client : set) {
                client.sendPacket(new ChannelStatusPacket(nameKey, ChannelStatus.CLOSED));
            }
        }
        return stream;
    }
    public Set<Client> getAllClientByChannel(SpacedNameKey channel){
        return clientMap.getOrDefault(channel, new HashSet<>());
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketReceivedEvent receivedEvent) {
            Packet packet = receivedEvent.getPacket();
            if (packet instanceof RegisterChannelPacket registerChannel) {
                var stream = channelMap.get(registerChannel.getChannelID());
                if (stream == null) {
                    serverWait.computeIfAbsent(registerChannel.getChannelID(), k -> new HashSet<>()).add(receivedEvent.getClient());
                    receivedEvent.getClient().sendPacket(new ChannelStatusPacket(registerChannel.getChannelID(), ChannelStatus.WAIT_SERVER));
                } else {
                    clientMap.computeIfAbsent(registerChannel.getChannelID(), k -> new HashSet<>()).add(receivedEvent.getClient());
                    receivedEvent.getClient().sendPacket(new ChannelStatusPacket(registerChannel.getChannelID(), ChannelStatus.OPENED));
                }
            } else if (packet instanceof ClientUnregisterChannelStreamPacket unregister) {
                clientMap.computeIfAbsent(unregister.getChannelID(), k -> new HashSet<>()).remove(receivedEvent.getClient());
                serverWait.computeIfAbsent(unregister.getChannelID(), k -> new HashSet<>()).remove(receivedEvent.getClient());
            } else if (packet instanceof ChanneledPacket channeledPacket) {
                ServerChannelStream stream = channelMap.get(channeledPacket.getChannelID());
                if (stream == null) {
                    LOGGER.severe(
                            String.format("Unknown channel '%s'! Client='%s', packet='%s'",
                                    channeledPacket.getChannelID(),
                                    receivedEvent.getClient().getId() + "[" + receivedEvent.getClient().getAddress() + "]",
                                    channeledPacket.getPacket().getClass().getSimpleName()
                            )
                    );
                    return;
                }
                stream.read(channeledPacket.getPacket(), receivedEvent.getClient());
            }
        } else if (event instanceof ClientDisconnectEvent disconnectEvent) {
            clientMap.values().forEach(l -> l.remove(disconnectEvent.getClient()));
            serverWait.values().forEach(l -> l.remove(disconnectEvent.getClient()));
        }
    }
}
