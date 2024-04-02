package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.Objects;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.CLIENT_BOUND)
public class DisconnectPacket extends Packet {
    private String reason;
    public DisconnectPacket() {
        super(PacketType.DISCONNECT);
    }
    public DisconnectPacket(String reason) {
        super(PacketType.DISCONNECT);
        this.reason = reason;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        reason = byteBuf.readUtf();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUtf(reason);
        return byteBuf;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "DisconnectPacket{" +
                "reason='" + reason + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisconnectPacket that = (DisconnectPacket) o;
        return Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason);
    }
}
