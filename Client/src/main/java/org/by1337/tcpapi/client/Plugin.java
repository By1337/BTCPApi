package org.by1337.tcpapi.client;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.event.EventListener;
import org.by1337.tcpapi.api.event.EventManager;
import org.by1337.tcpapi.client.event.AsyncDisconnectEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Level;

public final class Plugin extends JavaPlugin implements EventListener {

    private EventManager eventManager;
    private boolean tryReconnect;
    private boolean stopServerOnDisconnect;
    private String ip;
    private String password;
    private String serverId;
    private int port;
    private boolean isStopped;

    @Override
    public void onLoad() {
        File cfg = new File(getDataFolder() + "/config.yml");
        if (!cfg.exists()) {
            saveResource("config.yml", true);
        }
        try {
            YamlConfig config = new YamlConfig(cfg);
            ip = config.getAsString("ip");
            password = config.getAsString("password");
            serverId = config.getAsString("current-server-id");
            port = config.getAsInteger("port");
            tryReconnect = config.getAsBoolean("try-reconnect", false);
            stopServerOnDisconnect = config.getAsBoolean("stop-server-on-disconnect", false);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "failed to enable", e);
            Bukkit.getServer().shutdown();
            return;
        }
        eventManager = new EventManager();
        eventManager.registerListener(this);
        var ext = tryConnect();
        if (ext != null) {
            getLogger().log(Level.SEVERE, "failed to connect", ext);
            onDisconnect();
        }
    }

    @Nullable
    private Exception tryConnect() {
        try {
            Manager manager = new Manager(getLogger(), ip, port, password, serverId, false, eventManager);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private void onDisconnect() {
        if (isStopped) return;
        if (stopServerOnDisconnect) {
            Bukkit.getServer().shutdown();
        } else if (tryReconnect) {
            var ext = tryConnect();
            if (ext != null) {
                getLogger().log(Level.SEVERE, "failed to connect", ext);
                onDisconnect();
            }
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof AsyncDisconnectEvent) {
            onDisconnect();
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            var manager = Manager.getInstance();
            if (manager != null) {
                manager.tick();
            }
        }, 0, 1);

    }

    @Override
    public void onDisable() {
        isStopped = true;
        Manager.getInstance().stop();
    }

}
