package org.by1337.tcpapi.api.util;

import java.util.Objects;

public class SpacedNameKey {
    private final NameKey space;
    private final NameKey name;

    public SpacedNameKey(NameKey space, NameKey name) {
        this.space = space;
        this.name = name;
    }

    public SpacedNameKey(String space, String name) {
        this.space = new NameKey(space);
        this.name = new NameKey(name);
    }

    public SpacedNameKey(String spacedNameKey) {
        if (!spacedNameKey.contains(":")) throw new IllegalArgumentException("Expected <space>:<name>");
        String[] arr = spacedNameKey.split(":");
        if (arr.length != 2) throw new IllegalArgumentException("Expected <space>:<name>");
        this.space = new NameKey(arr[0]);
        this.name = new NameKey(arr[1]);
    }

    public NameKey getSpace() {
        return space;
    }

    public NameKey getName() {
        return name;
    }

    @Override
    public String toString() {
        return space.getName() + ":" + name.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpacedNameKey that = (SpacedNameKey) o;
        return Objects.equals(space, that.space) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(space, name);
    }
}
