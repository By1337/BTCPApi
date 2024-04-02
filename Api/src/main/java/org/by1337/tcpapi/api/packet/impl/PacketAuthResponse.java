package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.Objects;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.CLIENT_BOUND)
public class PacketAuthResponse extends Packet {
    private Response response;
    public PacketAuthResponse() {
        super(PacketType.AUTH_RESPONSE);
    }
    public PacketAuthResponse(Response response) {
        this();
        this.response = response;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        response = Response.values()[byteBuf.readVarInt()];
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeVarInt(response.ordinal());
        return byteBuf;
    }

    public Response getResponse() {
        return response;
    }

    public enum Response {
        SUCCESSFULLY,
        FAILED
    }

    @Override
    public String toString() {
        return "PacketAuthResponse{" +
                "response=" + response +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketAuthResponse that = (PacketAuthResponse) o;
        return response == that.response;
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }
}
