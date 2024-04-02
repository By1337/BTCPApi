package org.by1337.tcpapi.api.packet.impl;

import org.by1337.tcpapi.api.PacketFlow;
import org.by1337.tcpapi.api.io.ByteBuffer;
import org.by1337.tcpapi.api.packet.Packet;
import org.by1337.tcpapi.api.packet.PacketInfo;
import org.by1337.tcpapi.api.packet.PacketType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
@PacketInfo.PacketFlowInfo(packetFlow = PacketFlow.SERVER_BOUND)
public class PacketAuth extends Packet {
    private String id;
    private String password;
    public PacketAuth(String id, String password) {
        super(PacketType.AUTH);
        this.id = id;
        this.password = password;
    }

    public PacketAuth() {
        super(PacketType.AUTH);
    }

    @Override
    public void read(ByteBuffer byteBuf) throws IOException {
        id = byteBuf.readUtf();
        password = byteBuf.readUtf();
    }

    @Override
    public ByteBuffer write(ByteBuffer byteBuf) throws IOException {
        byteBuf.writeUtf(id);
        byteBuf.writeUtf(encode(password));
        return byteBuf;
    }

    @Nullable
    public String tryDecodePassword(String originalPss) {
        try {
            String s = new String(Base64.getDecoder().decode(password.getBytes(StandardCharsets.UTF_8)));
            if (s.length() != originalPss.length()) return null;

            char[] arr = s.toCharArray();

            StringBuilder out = new StringBuilder();

            for (int i = 0; i < arr.length; i++) {
                out.append((char) (arr[i] ^ originalPss.charAt(i)));
            }
            return out.reverse().toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String encode(String pass) {
        String reversedPassword = new StringBuilder(pass).reverse().toString();

        char[] arr = reversedPassword.toCharArray();

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            out.append((char) (arr[i] ^ pass.charAt(i)));
        }
        byte[] bytes = Base64.getEncoder().encode(out.toString().getBytes(StandardCharsets.UTF_8));

        return new String(bytes);
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "PacketAuth{" +
                "plugin='" + id + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketAuth that = (PacketAuth) o;
        return Objects.equals(id, that.id) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, password);
    }
}
