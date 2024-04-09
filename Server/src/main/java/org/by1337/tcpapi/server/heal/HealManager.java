package org.by1337.tcpapi.server.heal;

import org.by1337.tcpapi.server.util.CrashReport;

public class HealManager {
    private final PacketCounter packetCounter;
    private final TPSCounter tpsCounter;

    public HealManager() {
        packetCounter = new PacketCounter();
        tpsCounter = new TPSCounter();
    }

    public void tick(){
        packetCounter.tick();
        tpsCounter.tick();
    }
    public String report(){
        return "\nPackets:\n" + packetCounter + "\n\n" +
                "TPS: " + tpsCounter.tps() + "\n" +
                "Memory: " + CrashReport.getMemoryInfo();
    }

    public PacketCounter getPacketCounter() {
        return packetCounter;
    }

    public TPSCounter getTpsCounter() {
        return tpsCounter;
    }
}
