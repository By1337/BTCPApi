package org.by1337.tcpapi.server.addon;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AddonLogger extends Logger {
    private final String prefix;

    public AddonLogger(@NotNull Addon addon, @NotNull String moduleName, @NotNull Logger parent) {
        super(addon.getClass().getCanonicalName(), null);
        prefix = String.format("[%s] ", moduleName);
        setParent(parent);
        setLevel(Level.ALL);
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        logRecord.setMessage(prefix + logRecord.getMessage());
        super.log(logRecord);
    }
}
