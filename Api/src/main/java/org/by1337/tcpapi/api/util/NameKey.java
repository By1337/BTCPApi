package org.by1337.tcpapi.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * The NameKey class represents a unique key based on a name, ensuring its validity.
 * It is used to identify objects and entities with names in the application.
 */
public class NameKey {
    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private final String name;

    /**
     * Constructs a NameKey with the given name.
     *
     * @param name The name to be used as the key.
     */
    public NameKey(String name) {
        validate(name);
        this.name = name;
    }

    public static void validate(String input) {
        validate(input, () -> String.format("Invalid name. Must be [a-zA-Z0-9._-]: '%s'", input));
    }

    public static void validate(String input, Supplier<String> message) {
        if (!pattern.matcher(input).matches()) {
            throw new IllegalArgumentException(message.get());
        }
    }

    /**
     * Get the name associated with this NameKey.
     *
     * @return The name of the NameKey.
     */
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameKey nameKey)) return false;
        return Objects.equals(getName(), nameKey.getName());
    }

    public int compareTo(NameKey nameKey) {
        return name.compareTo(nameKey.name);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "NameKey{" +
                "name='" + name + '\'' +
                '}';
    }
}
