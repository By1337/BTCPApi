package org.by1337.tcpapi.server.logger;


import org.by1337.tcpapi.server.util.ColoredConsoleOutput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.logging.*;

public class LogManager {
    public static final LogManager instance = new LogManager();
    private final Logger logger = Logger.getLogger("main");

    private LogManager() {
        try {
            File logsFolder = new File("./logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdir();
            }

            File logFile = new File(logsFolder, "latest.log");

            if (logFile.exists()) {
                String fileName = getDateFormat(Calendar.getInstance());
                File renamedLogFile = new File(logsFolder, fileName + ".log");
                int x = 1;
                while (renamedLogFile.exists()) {
                    renamedLogFile = new File(logsFolder, fileName + "-" + x + ".log");
                    x++;
                }
                logFile.renameTo(renamedLogFile);
            }

            FileHandler fileHandler = new FileHandler("logs/latest.log");
            fileHandler.setFormatter(getFormatter());
            fileHandler.setEncoding(StandardCharsets.UTF_8.name());
            logger.addHandler(fileHandler);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(getColoredFormatter());

            consoleHandler.setEncoding(StandardCharsets.UTF_8.name());
            logger.addHandler(consoleHandler);


            logger.setUseParentHandlers(false);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Formatter getColoredFormatter() {
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                if (record.getLevel().equals(Level.SEVERE)) {
                    sb.append("\033[0;31m[");
                } else if (record.getLevel().equals(Level.WARNING)) {
                    sb.append("\033[0;33m[");
                } else {
                    sb.append("\033[0;37m[");
                }
                sb.append(getTimeFormat(Calendar.getInstance())).append(" ");

                sb.append(record.getLevel().getName()).append("] ");
                sb.append(ColoredConsoleOutput.applyColors(formatMessage(record))).append("\n");
                if (record.getThrown() != null) {
                    writeStackTrace(record.getThrown(), sb);
                }
                sb.append("\u001B[0m");
                return sb.toString();
            }
        };
    }

    public Formatter getFormatter() {
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder("[");

                sb.append(getTimeFormat(Calendar.getInstance())).append(" ");

                sb.append(record.getLevel().getName()).append("] ");
                sb.append(formatMessage(record)).append("\n");
                if (record.getThrown() != null) {
                    writeStackTrace(record.getThrown(), sb);
                }
                return sb.toString();
            }
        };
    }

    public static String getDateFormat(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);

        return year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day_of_month < 10 ? "0" + day_of_month : "" + day_of_month);

    }

    public static String getTimeFormat(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return (hour < 10 ? "0" + hour : "" + hour) + ":" +
                (minute < 10 ? "0" + minute : "" + minute) + ":" +
                (second < 10 ? "0" + second : "" + second);

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

    private static PrintStream oldOut;
    private static PrintStream oldErr;
    private static boolean hooked;

    public static void soutHook() {
        if (hooked) {
            throw new IllegalStateException("Sout is already hooked!");
        }
        oldOut = System.out;
        oldErr = System.err;

        System.setOut(new PrintStream(new SoutHook(false), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new SoutHook(true), true, StandardCharsets.UTF_8));
        hooked = true;
    }

    public static void soutUnhook() {
        if (!hooked) {
            throw new IllegalStateException("Sout isn't hooked!");
        }
        System.out.close();
        System.err.close();
        System.setOut(oldOut);
        System.setErr(oldErr);
        hooked = false;
    }

    public static void close() {
        if (hooked) {
            soutUnhook();
        }
        for (Handler handler : getLogger().getHandlers()) {
            handler.close();
        }
    }

    public static Logger getLogger() {
        return instance.logger;
    }


    private static class SoutHook extends ByteArrayOutputStream {
        private final Logger logger;
        private final boolean err;

        private SoutHook(boolean err) {
            this.err = err;
            if (err) {
                logger = getLogger();
            } else {
                logger = new MarkedLogger("SOUT", SoutHook.class);
            }

        }

        @Override
        public void flush() {
            String s = this.toString();
            if (!s.endsWith("\n")) {
                return; // todo System.out.print("something"); is ignored
            } else {
                s = s.substring(0, s.length() - 1);
            }
            if (err)
                logger.severe(s);
            else
                logger.info(s);
            reset();
        }
    }
}
