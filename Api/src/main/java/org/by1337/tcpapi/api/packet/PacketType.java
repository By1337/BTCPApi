package org.by1337.tcpapi.api.packet;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.packet.impl.*;
import org.by1337.tcpapi.api.packet.impl.channel.ChannelStatusPacket;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;
import org.by1337.tcpapi.api.packet.impl.channel.ClientUnregisterChannelStreamPacket;
import org.by1337.tcpapi.api.packet.impl.channel.RegisterChannelPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketType<T extends Packet> {
    private static final Object sync = new Object();
    private static final Map<Integer, PacketType<?>> types = new HashMap<>();

    public static final PacketType<PacketAuth> AUTH = new PacketType<>("auth", PacketAuth::new).setPacketFlow(PacketFlow.SERVER_BOUND);
    public static final PacketType<DisconnectPacket> DISCONNECT = new PacketType<>("disconnect", DisconnectPacket::new).setPacketFlow(PacketFlow.CLIENT_BOUND);
    public static final PacketType<PacketAuthResponse> AUTH_RESPONSE = new PacketType<>("auth_response", PacketAuthResponse::new).setPacketFlow(PacketFlow.CLIENT_BOUND);
    public static final PacketType<PacketPingRequest> PING_REQUEST = new PacketType<>("ping_request", PacketPingRequest::new);
    public static final PacketType<PacketPingResponse> PING_RESPONSE = new PacketType<>("ping_response", PacketPingResponse::new);
    public static final PacketType<ChanneledPacket> CHANNELED_PACKET = new PacketType<>("channeled_packet", ChanneledPacket::new);
    public static final PacketType<RegisterChannelPacket> REGISTER_CHANNEL_PACKET = new PacketType<>("register_channel_packet", RegisterChannelPacket::new).setPacketFlow(PacketFlow.SERVER_BOUND);
    public static final PacketType<ChannelStatusPacket> CHANNEL_STATUS_PACKET = new PacketType<>("channel_status_packet", ChannelStatusPacket::new).setPacketFlow(PacketFlow.CLIENT_BOUND);
    public static final PacketType<ClientUnregisterChannelStreamPacket> CLIENT_UNREGISTER_CHANNEL_STREAM_PACKET = new PacketType<>("client_unregister_channel_stream_packet", ClientUnregisterChannelStreamPacket::new).setPacketFlow(PacketFlow.SERVER_BOUND);

    private final Supplier<T> suppler;
    private final int id;
    private final String name;
    private PacketFlow packetFlow = PacketFlow.ANY;

    public PacketType(@NotNull String uniqueId, @NotNull Supplier<T> suppler) {
        synchronized (sync) {
            id = Arrays.hashCode(uniqueId.toCharArray());
            this.suppler = suppler;
            PacketType<?> type = types.get(id);
            if (type != null) {
                throw new IllegalStateException("a packet with this ID already exists! Current uniqueId='" + uniqueId + "' other uniqueId='" + type.name + "'");
            }
            name = uniqueId;
            types.put(id, this);
        }
    }

    @Nullable
    public static PacketType<?> byId(int id) {
        return types.get(id);
    }

    public int ordinal() {
        return getId();
    }

    public PacketFlow getPacketFlow() {
        return packetFlow;
    }

    public PacketType<T> setPacketFlow(PacketFlow packetFlow) {
        this.packetFlow = packetFlow;
        return this;
    }

    @NotNull
    public static PacketType<?>[] values() {
        return types.values().toArray(new PacketType[0]);
    }


    public Supplier<T> getSuppler() {
        return suppler;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Packet> T createNew(int id) {
        var type = byId(id);
        if (type == null) return null;
        return (T) type.suppler.get();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
