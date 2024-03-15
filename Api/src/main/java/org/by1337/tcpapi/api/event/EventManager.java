package org.by1337.tcpapi.api.event;

import org.by1337.tcpapi.api.util.LockableList;

public class EventManager {
    private final LockableList<EventListener> eventListeners = new LockableList<>();

    public void registerListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void unregisterListener(EventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public synchronized void callEvent(Event event) {
        eventListeners.lock();
        for (EventListener eventListener : eventListeners) {
            eventListener.onEvent(event);
        }
        eventListeners.unlock();
    }
    public void clearListeners(){
        eventListeners.clear();
    }
}
