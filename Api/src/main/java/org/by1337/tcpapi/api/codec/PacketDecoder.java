package org.by1337.tcpapi.api.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class PacketDecoder extends ByteToMessageDecoder {
    private final Logger logger;
    private final boolean debug;
    private final PacketFlow packetFlow;

    public PacketDecoder(Logger logger, PacketFlow packetFlow) {
        this.logger = logger;
        this.packetFlow = packetFlow;
        this.debug = false;
    }

    public PacketDecoder(boolean debug, Logger logger, PacketFlow packetFlow) {
        this.logger = logger;
        this.debug = debug;

        this.packetFlow = packetFlow;
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
        if (packet.getType().getPacketFlow() != PacketFlow.ANY && packet.getType().getPacketFlow() != packetFlow){
            throw new DecoderException("Incorrect packet flow detected! " + packet);
        }
        packet.read(byteBuf);
        if (debug) {
            logger.info(packet.getClass().getSimpleName());
        }
        return packet;
    }
}
