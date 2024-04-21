package org.by1337.tcp.velocityclient.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private final String ip;
    private final int port;
    private final String password;
    private final String serverId;
    private final Map<String, String> associations;

    public Config(File dataFolder) {
        dataFolder.mkdirs();
        File cfg = new File(dataFolder, "config.json");
        if (!cfg.exists()) {
            saveDefault(dataFolder);
        }
        try {
            CompoundTag compoundTag = NBTParser.parseAsCompoundTag(Files.readString(cfg.toPath()));
            ip = compoundTag.getAsString("ip");
            port = compoundTag.getAsInt("port");
            password = compoundTag.getAsString("password");
            serverId = compoundTag.getAsString("current-server-id");
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, NBT> entry : compoundTag.getAsCompoundTag("associations").getTags().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsObject().toString());
            }
            associations = Collections.unmodifiableMap(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDefault(File dataFolder) {
        try {
            URL url = this.getClass().getClassLoader().getResource("config.json");
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);


            File outFile = new File(dataFolder, "config.json");
            if (outFile.exists()) {
                outFile.delete();
            }
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(outFile)
            ) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "Config{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", serverId='" + serverId + '\'' +
                '}';
    }

    public Map<String, String> getAssociations() {
        return associations;
    }
}
