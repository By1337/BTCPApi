package org.by1337.tcp.velocityclient.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.PacketAuth;
import org.by1337.tcpapi.api.packet.impl.PacketAuthResponse;

import java.net.SocketAddress;

@ChannelHandler.Sharable
public class ConnectionAuth extends SimpleChannelInboundHandler<Packet> {
    private final Connection connection;

    public ConnectionAuth(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        PacketAuth auth = new PacketAuth(connection.getCfg().getServerId(), connection.getCfg().getPassword());
        ctx.writeAndFlush(auth);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        Channel channel = ctx.channel();
        SocketAddress address = channel.remoteAddress();
        connection.connect(ctx.channel(), address);

        if (packet instanceof PacketAuthResponse response) {
            if (response.getResponse() == PacketAuthResponse.Response.SUCCESSFULLY) {
                channel.pipeline().remove("auth");
                channel.pipeline().addLast("handler", connection);
            } else {
                connection.disconnect("failed register: " + packet);
            }
        } else {
            connection.disconnect("failed register: " + packet);
        }
    }
}