package org.by1337.tcpapi.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.by1337.tcpapi.api.event.EventManager;
import org.by1337.tcpapi.server.addon.AddonInitializer;
import org.by1337.tcpapi.server.addon.AddonLoader;
import org.by1337.tcpapi.server.config.ConfigManager;
import org.by1337.tcpapi.server.console.TcpConsole;
import org.by1337.tcpapi.server.heal.HealManager;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.network.Server;
import org.by1337.tcpapi.server.network.channel.ChannelStreamManager;
import org.by1337.tcpapi.server.task.Task;
import org.by1337.tcpapi.server.task.Ticker;
import org.by1337.tcpapi.server.util.OptionParser;
import org.by1337.tcpapi.server.util.TimeCounter;

import java.io.File;
import java.util.Objects;

public class ServerManager {
    private static ServerManager instance;
    private final EventManager eventManager;
    private final Ticker ticker;
    private final Server server;
    private final TcpConsole tcpConsole;
    private final AddonLoader addonLoader;
    private final HealManager healManager;
    private final ConfigManager configManager;
    private final ChannelStreamManager channelStreamManager;
    private final boolean debug;

    private ServerManager(int port, String password) {
        this(port, password, false);
    }

    private ServerManager(int port, String password, boolean debug) {
        this.debug = debug;
        instance = this;
        TimeCounter timeCounter = new TimeCounter();
        healManager = new HealManager();
        LogManager.soutHook();
        configManager = new ConfigManager();
        applyCfg();
        File dir = new File("./addons");
        if (!dir.exists()) {
            dir.mkdir();
        }
        addonLoader = new AddonLoader(LogManager.getLogger(), dir);
        eventManager = new EventManager();
        ticker = new Ticker();
        server = new Server(port, password);
        tcpConsole = new TcpConsole();
        channelStreamManager = new ChannelStreamManager();

        eventManager.registerListener(channelStreamManager);

        AddonInitializer addonInitializer = new AddonInitializer(addonLoader);
        addonInitializer.findAddons();
        addonInitializer.process();
        addonInitializer.onLoad();
        //   addonLoader.onLoadPingAll();
        server.start(debug);
        ticker.registerTask(new Task(true, 0, this::tick));
        addonInitializer.onEnable();
        // addonLoader.enableAll();
        LogManager.getLogger().info("Done in (" + timeCounter.getTimeFormat() + ")");
        new ThreadFactoryBuilder().setNameFormat("Terminal reader #%d").build().newThread(tcpConsole::start).start();
        ticker.start();
    }

    private void tick() {
        server.tick();
        healManager.tick();
    }

    public void stop() {
        ticker.stop();
        tcpConsole.setStopped(true);
        addonLoader.disableAll();
        addonLoader.unloadAll();
        server.shutdown();
        System.exit(0);
    }

    private void applyCfg() {
        System.setProperty("io.netty.eventLoopThreads", Integer.toString(configManager.getNettyThreads()));
    }

    public static ServerManager getInstance() {
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

    public static TcpConsole getTcpConsole() {
        return instance.tcpConsole;
    }

    public static AddonLoader getAddonLoader() {
        return instance.addonLoader;
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
        new ServerManager(port, password, debug);
    }

    public HealManager getHealManager() {
        return healManager;
    }

    public static ChannelStreamManager getChannelStreamManager() {
        return instance.channelStreamManager;
    }

    public static boolean isDebug() {
        return instance.debug;
    }
}

