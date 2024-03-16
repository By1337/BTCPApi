package org.by1337.tcpapi.api.event;

import org.by1337.tcpapi.api.util.LockableList;

public class EventManager {
    private final LockableList<EventListener> eventListeners = new LockableList<>();

    public void registerListener(EventListener eventListener) {
        synchronized (this) {
            eventListeners.add(eventListener);
        }
    }

    public void unregisterListener(EventListener eventListener) {
        synchronized (this) {
            eventListeners.remove(eventListener);
        }
    }

    public synchronized void callEvent(Event event) {
        synchronized (this) {
            eventListeners.lock();
            for (EventListener eventListener : eventListeners) {
                eventListener.onEvent(event);
            }
            eventListeners.unlock();
        }
    }

    public void clearListeners() {
        synchronized (this) {
            eventListeners.clear();
        }
    }
}
