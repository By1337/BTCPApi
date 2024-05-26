package org.by1337.tcpapi.server.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.server.ServerManager;
import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.yaml.YamlContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class ConfigManager {
    private final YamlContext config;
    private final Integer nettyThreads;

    public ConfigManager() {
       File cfg = saveResource("config.yml", false, new File("./"));
       try {
           config = new YamlContext(cfg);
       } catch (YamlContext.YamlParserException | IOException e) {
           throw new RuntimeException(e);
       }

       nettyThreads = config.get("netty-threads").getAsInteger();

    }

    public YamlContext getConfig() {
        return config;
    }

    public Integer getNettyThreads() {
        return nettyThreads;
    }

    @CanIgnoreReturnValue
    public File saveResource(@NotNull String resourcePath, boolean replace, File dataFolder) {
        InputStream in = getResource(resourcePath);
        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
            return outFile;
        } catch (IOException ex) {
            LogManager.getLogger().log(Level.SEVERE, "Could not saveAndPrint " + outFile.getName() + " to " + outFile, ex);
            throw new RuntimeException(ex);
        }
    }
    private @Nullable InputStream getResource(@NotNull String filename) {
        try {
            URL url = ServerManager.class.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
