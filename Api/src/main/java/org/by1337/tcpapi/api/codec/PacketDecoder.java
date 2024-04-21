package org.by1337.tcpapi.api.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketType;
import org.by1337.tcpapi.api.packet.impl.channel.ChanneledPacket;

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

    private Packet read(ByteBuffer byteBuf) throws IOException {
        Packet packet = byteBuf.readPacket();
        if (packet.getType().getPacketFlow() != PacketFlow.ANY && packet.getType().getPacketFlow() != packetFlow) {
            throw new DecoderException("Incorrect packet flow detected! " + packet);
        }
        if (debug) {
            if (packet instanceof ChanneledPacket channeledPacket) {
                logger.info(packet.getClass().getSimpleName() + " packet=" + channeledPacket.getPacket().getClass().getSimpleName() + " channel=" + channeledPacket.getChannelID());
            } else {
                logger.info(packet.getClass().getSimpleName());
            }
        }
        return packet;
    }
}
