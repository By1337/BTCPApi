package org.by1337.tcpapi.api.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final Logger logger;
    private final boolean debug;
    private final PacketFlow packetFlow;

    public PacketEncoder(boolean debug, Logger logger, PacketFlow packetFlow) {
        this.logger = logger;
        this.debug = debug;
        this.packetFlow = packetFlow;
    }

    public PacketEncoder(Logger logger, PacketFlow packetFlow) {
        this.logger = logger;
        this.packetFlow = packetFlow;
        this.debug = false;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf byteBuf) throws Exception {
        try {
            if (packet.getType().getPacketFlow() != PacketFlow.ANY && packet.getType().getPacketFlow() != packetFlow){
                throw new EncoderException("Incorrect packet flow detected! " + packet);
            }
            ByteBuffer buf = new ByteBuffer(byteBuf);
            buf.writeVarInt(packet.getType().getId());
            packet.write(buf);
            if (debug) {
                logger.info(packet.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to encode packet " + packet.getClass().getSimpleName(), e);
        }

    }
}
