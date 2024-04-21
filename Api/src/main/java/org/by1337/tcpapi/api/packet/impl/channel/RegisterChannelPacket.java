package org.by1337.tcpapi.api.packet.impl.channel;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;
import org.by1337.tcpapi.api.util.SpacedNameKey;

import java.io.IOException;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.SERVER_BOUND)
public class RegisterChannelPacket extends Packet {
    private SpacedNameKey channelID;

    public RegisterChannelPacket() {
        super(PacketType.REGISTER_CHANNEL_PACKET);
    }

    public RegisterChannelPacket(SpacedNameKey channelID) {
        this();
        this.channelID = channelID;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        channelID = byteBuf.readSpacedNameKey();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeSpacedNameKey(channelID);
        return byteBuf;
    }

    public SpacedNameKey getChannelID() {
        return channelID;
    }
}
