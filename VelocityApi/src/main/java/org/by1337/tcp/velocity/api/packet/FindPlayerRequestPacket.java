package org.by1337.tcp.velocity.api.packet;

import org.by1337.tcp.velocity.api.VelocityPacketRegistry;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.UUID;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.CLIENT_BOUND)
public class FindPlayerRequestPacket extends Packet {
    private UUID player;
    public FindPlayerRequestPacket() {
        super(VelocityPacketRegistry.FIND_PLAYER_REQUEST_PACKET);
    }

    public FindPlayerRequestPacket(UUID player) {
        this();
        this.player = player;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        player = byteBuf.readUUID();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUUID(player);
        return byteBuf;
    }

    public UUID getPlayer() {
        return player;
    }
}
