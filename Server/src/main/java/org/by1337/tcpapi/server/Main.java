package org.by1337.tcpapi.server;

import org.by1337.tcpapi.api.event.EventManager;
import org.by1337.tcpapi.server.addon.AddonLoader;
import org.by1337.tcpapi.server.console.TcpConsole;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.network.Server;
import org.by1337.tcpapi.server.task.Task;
import org.by1337.tcpapi.server.task.Ticker;
import org.by1337.tcpapi.server.util.OptionParser;
import org.by1337.tcpapi.server.util.TPSCounter;
import org.by1337.tcpapi.server.util.TimeCounter;

import java.io.File;
import java.util.Objects;

public class Main {
    private static Main instance;
    private final EventManager eventManager;
    private final Ticker ticker;
    private final Server server;
    private final TcpConsole tcpConsole;
    private final AddonLoader addonLoader;

    private Main(int port, String password) {
        this(port, password, false);
    }

    private Main(int port, String password, boolean debug) {
        TimeCounter timeCounter = new TimeCounter();
        instance = this;
        LogManager.soutHook();
        File dir = new File("./addons");
        if (!dir.exists()) {
            dir.mkdir();
        }
        addonLoader = new AddonLoader(LogManager.getLogger(), dir);
        eventManager = new EventManager();
        ticker = new Ticker();
        server = new Server(port, password);
        tcpConsole = new TcpConsole();
        addonLoader.loadAll();
        server.start(debug);
        ticker.registerTask(new Task(true, 0, this::tick));
        addonLoader.enableAll();
        LogManager.getLogger().info("Done in (" + timeCounter.getTimeFormat() + ")");
        new Thread(tcpConsole::start).start();
        ticker.start();
    }


    private void tick() {
        server.tick();
    }


    public void stop() {
        ticker.stop();
        addonLoader.disableAll();
        server.shutdown();
    }

    public static Main getInstance() {
        return instance;
    }

    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    public static Ticker getTicker() {
        return instance.ticker;
    }

    public static void isMainThread() {
        if (!instance.ticker.isMainThread()) {
            throw new IllegalStateException("is not the main thread!");
        }
    }

    public static Server getServer() {
        return instance.server;
    }

    public static void main(String[] args) {
        OptionParser parser = new OptionParser(String.join(" ", args));
        String sPort = parser.get("port");
        Objects.requireNonNull(sPort, "missing port!");
        int port = Integer.parseInt(sPort);
        String password = parser.get("pass");
        Objects.requireNonNull(password, "missing pass!");
        boolean debug = Boolean.parseBoolean(parser.getOrDefault("debug", "false"));
        LogManager.getLogger().info("using: " + parser);
        new Main(port, password, debug);
    }
}

