package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;

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
}
