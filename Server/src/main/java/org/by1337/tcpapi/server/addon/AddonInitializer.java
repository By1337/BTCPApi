package org.by1337.tcpapi.server.addon;

import org.by1337.tcpapi.server.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AddonInitializer {
    private final AddonLoader addonLoader;
    private List<Pair<File, AddonDescriptionFile>> toLoad;

    public AddonInitializer(AddonLoader addonLoader) {
        this.addonLoader = addonLoader;
    }

    public void process() {
        findAddons();
        removeDuplicates();
        checkDepend();
        checkCyclicalDepend();
        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            try {
                addonLoader.loadAddon(pair.getLeft(), pair.getRight());
            } catch (IOException | InvalidAddonException e) {
                addonLoader.getLogger().log(Level.SEVERE, "failed to load addon " + pair.getRight().getName(), e);
            }
        }
    }

    public void onLoad() {
        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            addonLoader.onLoadPing(pair.getRight().getName());
        }
    }

    public void onEnable() {
        // todo better sort through the collection
        Map<String, Pair<File, AddonDescriptionFile>> map = new HashMap<>();
        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            map.put(pair.getRight().getName(), pair);
        }
        var list = toLoad.stream().map(Pair::getRight).collect(Collectors.toList());
        while (!list.isEmpty()) {
            var iterator = list.iterator();
            main:
            while (iterator.hasNext()) {
                var addon = iterator.next();
                if (addon.getDepend().isEmpty() && addon.getSoftDepend().isEmpty()) {
                    addonLoader.enable(addon.getName());
                    iterator.remove();
                    continue;
                }
                if (!addon.getDepend().isEmpty()) {
                    for (String s : addon.getDepend()) {
                        if (!map.containsKey(s)) { // этого никогда не должно произойти...
                            addonLoader.getLogger().log(Level.SEVERE, String.format("Failed to enable %s! Depend not found '%s'", addon.getName(), s), new Throwable());
                            iterator.remove();
                            addonLoader.unload(addon.getName());
                            continue main;
                        }
                        Addon addon1 = addonLoader.getAddon(s);
                        if (addon1 == null) {
                            continue main;
                        }
                        if (!addon1.isEnabled()){
                            if (addon1.isTryTyEnable()){
                                addonLoader.getLogger().log(Level.SEVERE, String.format("Failed to enable %s! Depend not enabled '%s'", addon.getName(), s));
                                iterator.remove();
                            }
                            continue main;
                        }
                    }
                }
                for (String s : addon.getSoftDepend()) {
                    if (map.containsKey(s)){
                        Addon addon1 = addonLoader.getAddon(s);
                        if (addon1 != null) {
                            if (!addon1.isEnabled() && !addon1.isTryTyEnable()){
                                continue main;
                            }
                        }
                    }
                }
                addonLoader.enable(addon.getName());
                iterator.remove();
            }
        }
    }

    public void findAddons() {
        if (!addonLoader.getDir().exists()) return;
        File[] files = addonLoader.getDir().listFiles();
        if (files == null) return;
        toLoad = new ArrayList<>();
        for (File file : files) {
            if (!file.getName().endsWith(".jar") && file.isDirectory()) continue;
            try {
                toLoad.add(new Pair<>(
                        file,
                        new AddonDescriptionFile(AddonLoader.readFileContentFromJar(file.getPath()))
                ));
            } catch (IOException e) {
                addonLoader.getLogger().log(Level.SEVERE, "failed to read file " + file.getPath(), e);
            }
        }
    }

    public void removeDuplicates() {
        Map<String, List<Pair<File, AddonDescriptionFile>>> files = new HashMap<>();

        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            files.computeIfAbsent(pair.getRight().getName(), k -> new ArrayList<>()).add(pair);
        }
        List<Pair<File, AddonDescriptionFile>> result = new ArrayList<>();
        for (String name : files.keySet()) {
            var list = files.get(name);
            if (list.size() > 1) {
                StringBuilder message = new StringBuilder("Duplicates detected:");
                for (Pair<File, AddonDescriptionFile> pair : list) {
                    message.append(" ").append(pair.getRight().getName()).append(" ").append(pair.getRight().getVersion());
                    message.append("in '").append(pair.getLeft().getPath()).append("'");
                }
                addonLoader.getLogger().severe(message.toString());
            }
            result.add(list.get(0));
        }
        toLoad = result;
    }

    public void checkDepend() {
        Set<String> addons = new HashSet<>();
        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            addons.add(pair.getRight().getName());
        }
        List<Pair<File, AddonDescriptionFile>> result = new ArrayList<>();
        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            for (String s : pair.getRight().getDepend()) {
                if (!addons.contains(s)) {
                    addonLoader.getLogger().severe(String.format("Failed to load %s! Depend not found '%s'", pair.getRight().getName(), s));
                    continue;
                }
            }
            result.add(pair);
        }
        toLoad = result;
    }

    public void checkCyclicalDepend() {
        Map<String, Pair<File, AddonDescriptionFile>> map = new HashMap<>();

        for (Pair<File, AddonDescriptionFile> pair : toLoad) {
            map.put(pair.getRight().getName(), pair);
        }
        List<Pair<File, AddonDescriptionFile>> result = new ArrayList<>();

        main:
        for (String name : map.keySet()) {
            var pair = map.get(name);
            for (String s : pair.getRight().getDepend()) {
                var depend = map.get(s);
                if (depend.getRight().getDepend().contains(name)) {
                    addonLoader.getLogger().severe(String.format("A cyclic depend was found between %s and %s", name, s));
                    continue main;
                }
            }
            result.add(pair);
        }
        toLoad = result;
    }

}
