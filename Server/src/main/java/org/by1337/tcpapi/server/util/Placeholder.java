package org.by1337.tcpapi.server.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Placeholder {
    protected final Map<String, Supplier<String>> placeholders = new HashMap<>();

    public void registerPlaceholder(String placeholder, String str) {
        registerPlaceholder(placeholder, () -> str);
    }
    public void registerPlaceholder(String placeholder, Supplier<String> supplier) {
        placeholders.put(placeholder, supplier);
    }

    public void registerPlaceholders(Collection<Map.Entry<String, Supplier<String>>> list) {
        for (var entry : list) {
            registerPlaceholder(entry.getKey(), entry.getValue());
        }
    }


    public String replace(String string) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, Supplier<String>> entry : placeholders.entrySet()) {
            String placeholder = entry.getKey();
            int len = placeholder.length();
            int pos = sb.indexOf(placeholder);
            while (pos != -1) {
                sb.replace(pos, pos + len, entry.getValue().get());
                pos = sb.indexOf(placeholder, pos + len - 2);
            }
        }
        return sb.toString();
    }

    public Set<Map.Entry<String, Supplier<String>>> entrySet() {
        return placeholders.entrySet();
    }

}
