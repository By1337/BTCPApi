package org.by1337.tcpapi.server.util;

public class ColoredConsoleOutput {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\033[0;30m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE = "\033[0;37m";

    private final static Placeholder placeholder;

    static {
        placeholder = new Placeholder();
        placeholder.registerPlaceholder("&0", BLACK);
        placeholder.registerPlaceholder("&1", BLUE);
        placeholder.registerPlaceholder("&2", GREEN);
        placeholder.registerPlaceholder("&3", CYAN);
        placeholder.registerPlaceholder("&4", RED);
        placeholder.registerPlaceholder("&5", PURPLE);
        placeholder.registerPlaceholder("&6", YELLOW);
        placeholder.registerPlaceholder("&7", WHITE);
        placeholder.registerPlaceholder("&9", BLUE);
        placeholder.registerPlaceholder("&a", GREEN);
        placeholder.registerPlaceholder("&b", CYAN);
        placeholder.registerPlaceholder("&c", RED);
        placeholder.registerPlaceholder("&d", PURPLE);
        placeholder.registerPlaceholder("&e", YELLOW);
        placeholder.registerPlaceholder("&f", WHITE);
        placeholder.registerPlaceholder("&r", RESET);

    }


    public static String applyColors(String input) {
        return placeholder.replace(input) + RESET;
    }

    public static String toMinecraftCode(String hex) {
        return switch (hex) {
            case "#000000" -> "&0";
            case "#FF5555" -> "&c";
            case "#0000AA", "#5555FF" -> "&1";
            case "#FF55FF", "#AA00AA" -> "&5";
            case "#00AA00", "#55FF55" -> "&2";
            case "#FFFF55" -> "&e";
            case "#00AAAA", "#55FFFF" -> "&3";
            case "#FFFFFF" -> "&f";
            case "#FFAA00" -> "&6";
            case "#AAAAAA", "#555555" -> "&7";
            default -> "&r";
        };
    }
}
