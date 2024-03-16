package org.by1337.tcpapi.server.yaml.adapter;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.tcpapi.server.util.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class AdapterRegistry {
    private static final HashMap<Class<?>, Adapter<?>> adapters;

    @CanIgnoreReturnValue
    public static <T> boolean registerAdapter(Class<T> adapterClass, Adapter<? extends T> adapter) {
        if (hasAdapter(adapterClass)) {
            return false;
        }
        adapters.put(adapterClass, adapter);
        return true;
    }

    @CanIgnoreReturnValue
    public static <T> boolean unregisterAdapter(Class<T> adapterClass) {
        if (!hasAdapter(adapterClass)) {
            return false;
        }
        adapters.remove(adapterClass);
        return true;
    }

    @Contract("null, _ -> null")
    @SuppressWarnings("unchecked")
    public static <T> T getAs(@Nullable Object o, @NotNull Class<T> clazz) {
        if (o == null) return null;
        if (clazz.isAssignableFrom(o.getClass())) {
            return clazz.cast(o);
        }
        Validate.notNull(clazz);
        Adapter<T> adapter = (Adapter<T>) adapters.get(clazz);
        return adapter.deserialize(o);
    }

    public static boolean hasAdapter(Class<?> adapterClass) {
        return adapters.containsKey(adapterClass);
    }

    static {
        adapters = new HashMap<>();

        registerAdapter(Object.class, new AdapterBuilder<>().build());
        registerAdapter(String.class, new AdapterBuilder<String>().serialize(s -> s).deserialize(String::valueOf).build());

        registerAdapter(Number.class, new AdapterBuilder<Number>().serialize(s -> s).deserialize(o -> Double.parseDouble(String.valueOf(o))).build());
        registerAdapter(Byte.class, new AdapterBuilder<Byte>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).byteValue()).build());
        registerAdapter(Short.class, new AdapterBuilder<Short>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).shortValue()).build());
        registerAdapter(Integer.class, new AdapterBuilder<Integer>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).intValue()).build());
        registerAdapter(Long.class, new AdapterBuilder<Long>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).longValue()).build());
        registerAdapter(Double.class, new AdapterBuilder<Double>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).doubleValue()).build());
        registerAdapter(Float.class, new AdapterBuilder<Float>().serialize(s -> s).deserialize(o -> getAs(o, Number.class).floatValue()).build());

    }
}
