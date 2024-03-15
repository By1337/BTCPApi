package org.by1337.tcpapi.client;

import org.by1337.tcpapi.client.network.Connection;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Connection connection = new Connection("localhost", 8080,"id", "password", Logger.getLogger("test"));

        connection.start(true);

        connection.authWait();

        while (true){}

    }
}
