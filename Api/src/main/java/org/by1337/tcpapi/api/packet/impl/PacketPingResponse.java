package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.Objects;

public class PacketPingResponse extends Packet {
    private int ping;
    private int id;
    public PacketPingResponse() {
        super(PacketType.PING_RESPONSE);
    }

    public PacketPingResponse(int ping, int id) {
        this();
        this.ping = ping;
        this.id = id;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        ping = byteBuf.readVarInt();
        id = byteBuf.readVarInt();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeVarInt(ping);
        byteBuf.writeVarInt(id);
        return byteBuf;
    }

    public int getPing() {
        return ping;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PacketPingResponse{" +
                "ping=" + ping +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketPingResponse that = (PacketPingResponse) o;
        return ping == that.ping && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ping, id);
    }
}
