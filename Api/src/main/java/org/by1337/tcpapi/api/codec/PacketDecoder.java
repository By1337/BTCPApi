package org.by1337.tcpapi.api.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class PacketDecoder extends ByteToMessageDecoder {
    private final Logger logger;
    private final boolean debug;

    public PacketDecoder(Logger logger) {
        this.logger = logger;
        this.debug = false;
    }

    public PacketDecoder(boolean debug, Logger logger) {
        this.logger = logger;
        this.debug = debug;

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() == 0) return;

        list.add(read(new ByteBuffer(byteBuf)));
        if (byteBuf.readableBytes() > 0) {
            throw new IOException("bad packet!");
        }
    }

    private <T extends Packet> T read(ByteBuffer byteBuf) throws IOException {
        int id = byteBuf.readVarInt();
        T packet = PacketType.createNew(id);
        if (packet == null) {
            throw new IOException("Bad packet id " + id);
        }
        packet.read(byteBuf);
        if (debug) {
            logger.info(packet.getClass().getSimpleName());
        }
        return packet;
    }
}
