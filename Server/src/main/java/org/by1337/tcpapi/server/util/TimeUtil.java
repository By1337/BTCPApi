package org.by1337.tcpapi.server.util;

public class TimeUtil {
    public static String getFormat(int time) {
        int hour = time / 3600;
        int min = time % 3600 / 60;
        int sec = time % 60;
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
