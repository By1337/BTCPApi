package org.by1337.tcpapi.server.util;

import java.text.DecimalFormat;

public class NumberUtil {
    public static String format(double value){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }
}
