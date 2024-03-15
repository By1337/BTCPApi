package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;

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
}
