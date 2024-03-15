package org.by1337.tcpapi.client;

import org.by1337.tcpapi.api.event.EventManager;
import org.by1337.tcpapi.client.network.Connection;

import java.util.logging.Logger;

public class Manager {
    private static Manager instance;
    private final Connection connection;
    private final Logger logger;
    private final EventManager eventManager;
    private final Thread mainThread;

    public Manager(Logger logger, String ip, int port, String password, String id, EventManager eventManager) {
        this(logger, ip, port, password, id, false, eventManager);
    }

    public Manager(Logger logger, String ip, int port, String password, String id, boolean debug, EventManager eventManager) {
        this.eventManager = eventManager;
        if (instance != null){
            throw new UnsupportedOperationException();
        }
        instance = this;
        mainThread = Thread.currentThread();
        this.logger = logger;
        connection = new Connection(ip, port, id, password, logger);
        connection.start(debug);
        connection.authWait(10_000L);
        if (!connection.isAuthorized()) {
            throw new IllegalStateException("connect failed!");
        }
    }
    public void tick(){
        connection.tick();
    }
    public void stop(){
        connection.shutdown();
    }
    public static void isMainThread() {
        if (instance.mainThread != Thread.currentThread()) {
            throw new IllegalStateException("is not the main thread!");
        }
    }

    public static Connection getConnection() {
        return instance.connection;
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    public static Manager getInstance() {
        return instance;
    }
}

