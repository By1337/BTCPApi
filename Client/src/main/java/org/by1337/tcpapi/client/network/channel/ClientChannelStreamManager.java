package org.by1337.tcpapi.client.network.channel;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.event.EventListener;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.channel.ChannelStatusPacket;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;
import org.by1337.tcpapi.api.util.SpacedNameKey;
import org.by1337.tcpapi.client.Manager;
import org.by1337.tcpapi.client.event.AsyncPacketReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientChannelStreamManager implements EventListener {
    private final Map<SpacedNameKey, ClientChannelStream> channels = new ConcurrentHashMap<>();

    public ClientChannelStream registerChannelStream(SpacedNameKey id, ChannelStreamPacketReader reader) {
        if (channels.containsKey(id)) {
            throw new IllegalStateException("channel stream already exist!");
        }
        ClientChannelStream stream = new ClientChannelStream(id, reader, this);
        channels.put(id, stream);
        return stream;
    }

    @Nullable
    @CanIgnoreReturnValue
    ClientChannelStream unregister(SpacedNameKey nameKey) {
        return channels.remove(nameKey);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof AsyncPacketReceivedEvent asyncPacketReceivedEvent) {
            Packet packet = asyncPacketReceivedEvent.getPacket();
            if (packet instanceof ChanneledPacket channeledPacket) {
                var stream = channels.get(channeledPacket.getChannelID());
                if (stream == null) {
                    Manager.getLogger().severe("Unknown channel " + channeledPacket.getChannelID());
                    return;
                }
                stream.read(channeledPacket.getPacket());
            } else if (packet instanceof ChannelStatusPacket statusPacket) {
                var stream = channels.get(statusPacket.getChannelID());
                if (stream == null) {
                    Manager.getLogger().severe("Unknown channel " + statusPacket.getChannelID());
                    return;
                }
                stream.setStatus(statusPacket.getStatus());
            }
        }
    }
}
