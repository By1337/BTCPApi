package org.by1337.tcp.velocity.api.packet;

import org.by1337.tcp.velocity.api.VelocityPacketRegistry;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.SERVER_BOUND)
public class PlayerConnectToServerPacket extends Packet {
    private UUID player;
    private String server;
    private @Nullable String previousServer;

    public PlayerConnectToServerPacket() {
        super(VelocityPacketRegistry.PLAYER_CONNECT_TO_SERVER_PACKET);
    }

    public PlayerConnectToServerPacket(UUID player, String server, @Nullable String previousServer) {
        this();
        this.player = player;
        this.server = server;
        this.previousServer = previousServer;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        player = byteBuf.readUUID();
        server = byteBuf.readUtf();
        previousServer = byteBuf.readOptional(ByteBuffer::readUtf).orElse(null);
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUUID(player);
        byteBuf.writeUtf(server);
        byteBuf.writeOptional(previousServer, ByteBuffer::writeUtf);
        return byteBuf;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getServer() {
        return server;
    }

    public Optional<String> getPreviousServer() {
        return Optional.ofNullable(previousServer);
    }

    @Override
    public String toString() {
        return "PlayerConnectToServerPacket{" +
                "player=" + player +
                ", server='" + server + '\'' +
                ", previousServer='" + previousServer + '\'' +
                '}';
    }
}
