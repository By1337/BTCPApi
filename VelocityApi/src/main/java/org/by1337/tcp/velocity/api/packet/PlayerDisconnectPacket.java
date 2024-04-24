package org.by1337.tcp.velocity.api.packet;

import org.by1337.tcp.velocity.api.VelocityPacketRegistry;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.SERVER_BOUND)
public class PlayerDisconnectPacket extends Packet {
    private UUID player;
    private @Nullable String fromServer;

    public PlayerDisconnectPacket() {
        super(VelocityPacketRegistry.PLAYER_DISCONNECT_PACKET);
    }

    public PlayerDisconnectPacket(UUID player, @Nullable String fromServer) {
        this();
        this.player = player;
        this.fromServer = fromServer;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        player = byteBuf.readUUID();
        fromServer = byteBuf.readOptional(ByteBuffer::readUtf).orElse(null);
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUUID(player);
        byteBuf.writeOptional(fromServer, ByteBuffer::writeUtf);
        return byteBuf;
    }

    public UUID getPlayer() {
        return player;
    }

    public Optional<String> getFromServer() {
        return Optional.ofNullable(fromServer);
    }

    @Override
    public String toString() {
        return "PlayerDisconnectPacket{" +
                "player=" + player +
                ", fromServer='" + fromServer + '\'' +
                '}';
    }
}
