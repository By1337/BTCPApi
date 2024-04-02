package org.by1337.tcpapi.api.packet;

import org.by1337.tcpapi.api.PacketFlow;

import java.lang.annotation.*;


public final class PacketInfo {
    @Documented
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.TYPE})
    public @interface PacketFlowInfo {
        PacketFlow packetFlow() default PacketFlow.ANY;
    }
}
