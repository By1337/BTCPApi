package org.by1337.tcpapi.api.packet;

import org.by1337.tcpapi.api.packet.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketType<T extends Packet> {
    private static final Object sync = new Object();
    private static final Map<Integer, PacketType<?>> types = new HashMap<>();

    public static final PacketType<PacketAuth> AUTH = new PacketType<>("auth", PacketAuth::new);
    public static final PacketType<DisconnectPacket> DISCONNECT = new PacketType<>("disconnect", DisconnectPacket::new);
    public static final PacketType<PacketAuthResponse> AUTH_RESPONSE = new PacketType<>("auth_response", PacketAuthResponse::new);
    public static final PacketType<PacketPingRequest> PING_REQUEST = new PacketType<>("ping_request", PacketPingRequest::new);
    public static final PacketType<PacketPingResponse> PING_RESPONSE = new PacketType<>("ping_response", PacketPingResponse::new);

    private final Supplier<T> suppler;
    private final int id;
    private final String name;
    public PacketType(@NotNull String uniqueId, @NotNull Supplier<T> suppler) {
        synchronized (sync){
            id = Arrays.hashCode(uniqueId.toCharArray());
            this.suppler = suppler;
            if (types.containsKey(id)) {
                throw new IllegalStateException("a packet with this ID already exists! uniqueId='" + uniqueId + "'");
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
