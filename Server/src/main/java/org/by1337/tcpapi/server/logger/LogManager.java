package org.by1337.tcpapi.server.logger;


import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.util.ColoredConsoleOutput;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.logging.*;

public class LogManager {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogManager.class);
    private LogManager() {
    }

    public static String getDateFormat(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);

        return year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day_of_month < 10 ? "0" + day_of_month : "" + day_of_month);
    }

    public static void writeStackTrace(Throwable throwable, StringBuilder sb) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        sb.append(stringWriter);

        try {
            stringWriter.close();
            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Logger getLogger() {
        return Logger.getGlobal();
    }
    public static org.slf4j.Logger getSLF4JLogger() {
        return LOGGER;
    }

}
