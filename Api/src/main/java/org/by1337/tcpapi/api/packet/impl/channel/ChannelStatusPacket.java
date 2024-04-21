package org.by1337.tcpapi.api.packet.impl.channel;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;
import org.by1337.tcpapi.api.util.SpacedNameKey;

import java.io.IOException;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.CLIENT_BOUND)
public class ChannelStatusPacket extends Packet {
    private SpacedNameKey channelID;
    private ChannelStatus status;

    public ChannelStatusPacket() {
        super(PacketType.CHANNEL_STATUS_PACKET);
    }

    public ChannelStatusPacket(SpacedNameKey channelID, ChannelStatus status) {
        this();
        this.channelID = channelID;
        this.status = status;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        channelID = byteBuf.readSpacedNameKey();
        status = byteBuf.readEnum(ChannelStatus.class);
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeSpacedNameKey(channelID);
        byteBuf.writeEnum(status);
        return byteBuf;
    }

    public SpacedNameKey getChannelID() {
        return channelID;
    }

    public ChannelStatus getStatus() {
        return status;
    }
}
