package org.by1337.tcpapi.api.event;

import org.by1337.tcpapi.api.util.LockableList;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final LockableList<EventListener> eventListeners = LockableList.createThreadSaveList();

    public void registerListener(EventListener eventListener) {
        eventListeners.add(eventListener);

    }

    public void unregisterListener(EventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void callEvent(Event event) {
        eventListeners.lock();
        for (EventListener eventListener : eventListeners) {
            eventListener.onEvent(event);
        }
        eventListeners.unlock();
    }

    public void clearListeners() {
        eventListeners.clear();
    }
}
