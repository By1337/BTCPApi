package org.by1337.tcpapi.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.impl.DisconnectPacket;
import org.by1337.tcpapi.api.packet.impl.PacketAuth;
import org.by1337.tcpapi.api.packet.impl.PacketAuthResponse;
import org.by1337.tcpapi.server.Main;

import java.net.SocketAddress;
import java.util.Objects;

@ChannelHandler.Sharable
public class UnregisteredConnection extends SimpleChannelInboundHandler<Packet> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PacketAuth auth) {
            Server server = Main.getServer();

            if (Objects.equals(server.getPassword(), auth.tryDecodePassword(server.getPassword()))) {
                try {
                    Channel channel = ctx.channel();
                    SocketAddress address = channel.remoteAddress();

                    channel.pipeline().remove("auth");
                    Connection connection = new Connection(channel, address, auth.getId(), server);
                    channel.pipeline().addLast("handler", connection);

                    server.registerConnection(connection);

                    connection.sendPacket(new PacketAuthResponse(PacketAuthResponse.Response.SUCCESSFULLY));
                } catch (Throwable t) {
                    DisconnectPacket packet1 = new DisconnectPacket(t.getLocalizedMessage());
                    ctx.channel().writeAndFlush(packet1);
                    ctx.channel().close();
                }
            } else {
                DisconnectPacket packet1 = new DisconnectPacket("wrong password!");
                ctx.channel().writeAndFlush(packet1);
                ctx.channel().close();
            }
        } else {
            DisconnectPacket packet1 = new DisconnectPacket("unauthorized!");
            ctx.channel().writeAndFlush(packet1);
            ctx.channel().close();
        }
    }
}
