package org.by1337.tcpapi.client;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.tcpapi.api.event.Event;
import org.by1337.tcpapi.api.event.EventListener;
import org.by1337.tcpapi.client.event.AsyncDisconnectEvent;

import java.io.File;
import java.util.logging.Level;

public final class Plugin extends JavaPlugin implements EventListener {

    @Override
    public void onLoad() {
        File cfg = new File(getDataFolder() + "/config.yml");
        if (!cfg.exists()) {
            saveResource("config.yml", true);
        }
        try {
            YamlConfig config = new YamlConfig(cfg);
            String ip = config.getContext().getAsString("ip");
            String password = config.getContext().getAsString("password");
            String serverId = config.getContext().getAsString("current-server-id");
            int port = config.getContext().getAsInteger("port");

            new Manager(getLogger(), ip, port, password, serverId);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "failed to enable", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof AsyncDisconnectEvent){
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        Manager.getInstance().stop();
    }

}
