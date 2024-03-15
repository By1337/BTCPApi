package org.by1337.tcpapi.server;

import org.by1337.tcpapi.api.event.EventManager;
import org.by1337.tcpapi.server.console.TcpConsole;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.network.Server;
import org.by1337.tcpapi.server.task.Task;
import org.by1337.tcpapi.server.task.Ticker;
import org.by1337.tcpapi.server.util.TPSCounter;
import org.by1337.tcpapi.server.util.TimeCounter;

public class Main {
    private static Main instance;
    private final EventManager eventManager;
    private final Ticker ticker;
    private final Server server;
    private final TcpConsole tcpConsole;


    private Main(int port, String password) {
        this(port, password, false);
    }

    private Main(int port, String password, boolean debug) {
        TimeCounter timeCounter = new TimeCounter();
        instance = this;
        LogManager.soutHook();
        eventManager = new EventManager();
        ticker = new Ticker();
        server = new Server(port, password);
        tcpConsole = new TcpConsole();
        server.start(debug);
        ticker.registerTask(new Task(true, 0, this::tick));
        LogManager.getLogger().info("Done in (" + timeCounter.getTimeFormat() + ")");
        new Thread(tcpConsole::start).start();
        ticker.start();
    }


    private void tick() {
        server.tick();
    }


    public void stop() {
        ticker.stop();
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
        new Main(8080, "password", true);

    }
}

