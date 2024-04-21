package org.by1337.tcp.velocity.api.packet;

import org.by1337.tcp.velocity.api.VelocityPacketRegistry;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class FindPlayerResponsePacket extends Packet {
    private UUID player;
    @Nullable
    private String server;
    public FindPlayerResponsePacket() {
        super(VelocityPacketRegistry.FIND_PLAYER_RESPONSE_PACKET);
    }

    public FindPlayerResponsePacket(UUID player, @Nullable String server) {
        this();
        this.player = player;
        this.server = server;
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        player = byteBuf.readUUID();
        server = byteBuf.readOptional(ByteBuffer::readUtf).orElse(null);
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUUID(player);
        byteBuf.writeOptional(server, ByteBuffer::writeUtf);
        return byteBuf;
    }

    public UUID getPlayer() {
        return player;
    }

    public Optional<String> getServer() {
        return Optional.ofNullable(server);
    }
}
