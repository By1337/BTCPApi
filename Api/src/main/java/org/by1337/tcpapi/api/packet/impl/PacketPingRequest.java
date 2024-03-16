package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.Objects;

public class PacketPingRequest extends Packet {
    private long time;
    private int id;
    public PacketPingRequest() {
        super(PacketType.PING_REQUEST);
    }

    public PacketPingRequest(long time, int id) {
        this();
        this.time = time;
        this.id = id;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        time = byteBuf.readVarLong();
        id = byteBuf.readVarInt();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeVarLong(time);
        byteBuf.writeVarInt(id);
        return byteBuf;
    }

    public long getTime() {
        return time;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PacketPingRequest{" +
                "time=" + time +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketPingRequest that = (PacketPingRequest) o;
        return time == that.time && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, id);
    }
}
