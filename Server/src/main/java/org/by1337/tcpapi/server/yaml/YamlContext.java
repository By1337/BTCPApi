package org.by1337.tcpapi.server.yaml;

import org.by1337.tcpapi.server.util.Validate;
import org.by1337.tcpapi.server.yaml.adapter.AdapterRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlContext {
    private LinkedHashMap<String, Object> raw;

    public YamlContext() {
        raw = new LinkedHashMap<>();
    }

    public YamlContext(String str) throws YamlParserException {
        try {
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(options);
            raw = yaml.load(str);
        } catch (Exception e) {
            throw new YamlParserException(e);
        }
    }

    public YamlContext(File file) throws YamlParserException, IOException {
        this(Files.readString(file.toPath()));
    }


    public String getAsString(@NotNull String path, @NotNull String def) {
        return AdapterRegistry.getAs(get(path, def), String.class);
    }

    @Nullable
    public String getAsString(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), String.class);
    }

    public Number getAsNumber(@NotNull String path, @NotNull Number def) {
        return AdapterRegistry.getAs(get(path, def), Number.class);
    }

    @Nullable
    public Number getAsNumber(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Number.class);
    }

    public Byte getAsByte(@NotNull String path, @NotNull Byte def) {
        return AdapterRegistry.getAs(get(path, def), Byte.class);
    }

    @Nullable
    public Byte getAsByte(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Byte.class);
    }

    public Short getAsShort(@NotNull String path, @NotNull Short def) {
        return AdapterRegistry.getAs(get(path, def), Short.class);
    }

    @Nullable
    public Short getAsShort(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Short.class);
    }

    @NotNull
    public Integer getAsInt(@NotNull String path, @NotNull Integer def) {
        return AdapterRegistry.getAs(get(path, def), Integer.class);
    }

    @Nullable
    public Integer getAsInt(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Integer.class);
    }

    public Long getAsLong(@NotNull String path, @NotNull Long def) {
        return AdapterRegistry.getAs(get(path, def), Long.class);
    }

    @Nullable
    public Long getAsLong(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Long.class);
    }

    public Double getAsDouble(@NotNull String path, @NotNull Double def) {
        return AdapterRegistry.getAs(get(path, def), Double.class);
    }

    @Nullable
    public Double getAsDouble(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Double.class);
    }

    public Float getAsFloat(@NotNull String path, @NotNull Float def) {
        return AdapterRegistry.getAs(get(path, def), Float.class);
    }

    @Nullable
    public Float getAsFloat(@NotNull String path) {
        return AdapterRegistry.getAs(get(path), Float.class);
    }

    @Nullable
    public <T> List<T> getList(@NotNull String path, @NotNull Class<T> type) {
        List<T> out = new ArrayList<>();
        List<?> raw = (List<?>) get(path);
        if (raw == null) return null;
        for (Object o : raw) {
            out.add(AdapterRegistry.getAs(o, type));
        }
        return out;
    }
    @Nullable
    public List<String> getListString(@NotNull String path){
        return getList(path, String.class);
    }

    public Object get(@NotNull String path, Object def) {
        var obj = get(path);
        return obj == null ? def : obj;
    }

    @Nullable
    public Object get(@NotNull String path) {
        Validate.notNull(path, "path is null!");
        Validate.test(path, String::isEmpty, () -> new IllegalStateException("path is empty"));
        String[] path0 = path.split("\\.");

        Object last = null;
        for (String s : path0) {
            if (last == null) {
                Object o = raw.get(s);
                if (o == null) return null;
                last = o;
            } else if (last instanceof Map<?, ?> sub) {
                Object o = sub.get(s);
                if (o == null) return null;
                last = o;
            } else {
                throw new ClassCastException(last.getClass().getName() + " to Map<String, Object>");
            }
        }
        return last;
    }


    public static class YamlParserException extends Exception {
        public YamlParserException() {
        }

        public YamlParserException(String message) {
            super(message);
        }

        public YamlParserException(String message, Throwable cause) {
            super(message, cause);
        }

        public YamlParserException(Throwable cause) {
            super(cause);
        }

        public YamlParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
