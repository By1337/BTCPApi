package org.by1337.tcpapi.server.util;


import org.jetbrains.annotations.Nullable;

public interface CallBack<T> {
    void back(@Nullable T value);
}
