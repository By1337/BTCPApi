package org.by1337.tcp.velocity.api;

import org.by1337.tcp.velocity.api.packet.*;
import org.by1337.tcpapi.api.packet.PacketType;

public class VelocityPacketRegistry {
    //public static final PacketType<UserInfoPacket> USER_INFO_PACKET = new PacketType<>("binvmanager:user_info_packet", UserInfoPacket::new);
    public static final PacketType<FindPlayerRequestPacket> FIND_PLAYER_REQUEST_PACKET = new PacketType<>("velocity:find_player_request_packet", FindPlayerRequestPacket::new);
    public static final PacketType<FindPlayerResponsePacket> FIND_PLAYER_RESPONSE_PACKET = new PacketType<>("velocity:find_player_response_packet", FindPlayerResponsePacket::new);
    public static final PacketType<PlayerConnectToServerPacket> PLAYER_CONNECT_TO_SERVER_PACKET = new PacketType<>("velocity:player_connect_to_server_packet", PlayerConnectToServerPacket::new);
    public static void load(){
        // ping static block
    }
}
