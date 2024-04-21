package org.by1337.tcpapi.api.packet.impl.channel;

import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;
import org.by1337.tcpapi.api.util.SpacedNameKey;

import java.io.IOException;

public class ChanneledPacket extends Packet {
    private Packet packet;
    private SpacedNameKey channelID;

    public ChanneledPacket() {
        super(PacketType.CHANNELED_PACKET);
    }

    public ChanneledPacket(Packet packet, SpacedNameKey channelID) {
        this();
        this.packet = packet;
        this.channelID = channelID;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        channelID = byteBuf.readSpacedNameKey();
        packet = byteBuf.readPacket();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeSpacedNameKey(channelID);
        byteBuf.writePacket(packet);
        return byteBuf;
    }

    public Packet getPacket() {
        return packet;
    }

    public SpacedNameKey getChannelID() {
        return channelID;
    }
}
