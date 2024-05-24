package org.by1337.tcpapi.server.logger;


import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MarkedLogger extends Logger {

    private final String prefix;
    public MarkedLogger(String prefix, Class<?> clazz) {
        super(clazz.getCanonicalName(), null);
        this.prefix = "[" + prefix + "] ";
        setLevel(Level.ALL);
        setParent(Logger.getGlobal());
    }
    @Override
    public void log(@NotNull LogRecord logRecord) {
        logRecord.setMessage(prefix + logRecord.getMessage());
        super.log(logRecord);
    }
}
