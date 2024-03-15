package org.by1337.tcpapi.api.packet;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.api.io.ByteBuffer;


import java.io.IOException;

public abstract class Packet {
    private final PacketType<?> type;

    public Packet(PacketType<?> type) {
        this.type = type;
    }

    public PacketType<?> getType() {
        return type;
    }
    public abstract void read(ByteBuffer byteBuf) throws IOException;
    @CanIgnoreReturnValue
    public abstract ByteBuffer write(ByteBuffer byteBuf) throws IOException;
}
