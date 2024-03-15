package org.by1337.tcpapi.server.addon;

import org.by1337.tcpapi.server.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddonLoader {
    private final Logger logger;
    private final File dir;
    private final Map<String, JavaAddon> addons = new ConcurrentHashMap<>();
    private final List<AddonClassLoader> loaders = new CopyOnWriteArrayList<>();
    private final List<URLClassLoader> libs = new CopyOnWriteArrayList<>();

    public AddonLoader(@NotNull Logger logger, @NotNull File dir) {
        this.logger = logger;
        this.dir = dir;
    }

    public void loadAll() {
        if (!dir.exists()) return;

        for (File file : dir.listFiles()) {
            if (!file.getName().endsWith(".jar") && file.isDirectory()) continue;
            try {
                loadAddon(file);
            } catch (InvalidAddonException | IOException e) {
                logger.log(Level.SEVERE, "failed to load addon: " + file.getName(), e);
            }
        }
    }

    public void enable(String name) {
        JavaAddon module = getAddon(name);
        if (module == null) {
            throw new IllegalArgumentException("unknown addon: " + name);
        }
        if (module.isEnabled()) return;
        module.getLogger().info("enabling...");
        try {
            module.setEnabled(true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to enable addon: " + module.getName(), e);
            disable(name);
        }
    }

    public void disable(String name) {
        JavaAddon module = getAddon(name);
        if (module == null) {
            throw new IllegalArgumentException("unknown addon: " + name);
        }
        if (!module.isEnabled()) return;
        module.getLogger().info("disabling...");
        try {
            module.setEnabled(false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to disable addon: " + module.getName(), e);
        }
    }

    public void unloadAll() {
        new ArrayList<>(addons.keySet()).forEach(this::unload);
    }

    public void unload(String name) {
        JavaAddon module = getAddon(name);
        if (module == null) {
            throw new IllegalArgumentException("unknown addon: " + name);
        }
        if (module.isEnabled()) {
            disable(name);
        }
        if (module.getClassLoader() instanceof AddonClassLoader loader) {
            try {
                loader.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
            }
            loaders.remove(loader);
        }
        addons.remove(name);

    }

    public void enableAll() {
        addons.keySet().forEach(this::enable);
    }

    public void disableAll() {
        addons.keySet().forEach(this::disable);
    }

    private void removeAddon(String name) {
        addons.remove(name);
    }

    @Nullable
    public JavaAddon getAddon(String name) {
        return addons.get(name);
    }

    public void loadAddon(File file) throws IOException, InvalidAddonException {
        AddonDescriptionFile description = new AddonDescriptionFile(readFileContentFromJar(file.getPath()));

        if (addons.containsKey(description.getName())) {
            throw new InvalidAddonException("duplicated addon %s!", description.getName());
        }

        File dataFolder = new File(dir + "/" + description.getName());

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        AddonClassLoader loader = new AddonClassLoader(Main.class.getClassLoader(), description, dataFolder, file, logger, this);
        JavaAddon addon = loader.getAddon();
        addons.put(addon.getName(), addon);
        loaders.add(loader);
        addon.onLoad();
    }

    public static String readFileContentFromJar(String jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry("addon.json");
            if (entry != null) {
                try (InputStream inputStream = jarFile.getInputStream(entry);
                     InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append(System.lineSeparator());
                    }
                    return content.toString();
                }
            } else {
                throw new IOException("Jar does not contain addon.json");
            }
        }
    }

    public String getAddonList() {
        StringBuilder sb = new StringBuilder();
        sb.append("&aAddons (").append(addons.size()).append("):");
        if (addons.isEmpty()) return sb.toString();
        sb.append(" ");
        addons.values().forEach(module -> {
            if (module.isEnabled()) sb.append("&a");
            else sb.append("&c");
            sb.append(module.getName()).append("&f, ");
        });
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    @Nullable
    public Class<?> getClassByName(String name, boolean resolve) {
        for (AddonClassLoader loader1 : loaders) {
            try {
                return loader1.loadClass0(name, resolve, false);
            } catch (ClassNotFoundException ignore) {
            }
        }

        return findClassInLibs(name);
    }

    @Nullable
    public Class<?> findClassInLibs(String name) {
        for (URLClassLoader lib : libs) {
            try {
                return lib.loadClass(name);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return null;
    }


    public void loadLib(File jar) throws MalformedURLException {
        logger.info("load lib " + jar.getName() + "...");
        libs.add(new URLClassLoader(new URL[]{jar.toURI().toURL()}, Main.class.getClassLoader()));
    }

    public boolean isLibLoader(URLClassLoader urlClassLoader){
        return libs.contains(urlClassLoader);
    }

    public void closeAndClearLibs() {
        for (URLClassLoader lib : libs) {
            try {
                lib.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "failed to unload lib!", e);
            }
        }
        libs.clear();
    }

    public Collection<JavaAddon> getAddons() {
        return addons.values();
    }
}
