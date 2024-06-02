# BTCPApi

BTCPApi is a simple API for communication between a server and a client via TCP. This library was primarily created for data synchronization on Minecraft servers.

## Synchronization Architecture

All synchronization is built on the following system:

`client-plugin` -> `BTCP-plugin` -> `BTCP-server` -> `server-plugin`

## Installation

To use BTCPApi in your project, add the following dependencies to your `pom.xml`:

```xml
<project>
    <repositories>
        <repository>
            <id>by1337</id>
            <url>https://repo.by1337.space/repository/maven-releases/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.by1337.tcpapi.api</groupId>
            <artifactId>BTCP-Api</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
        <!--Client-->
        <dependency>
            <groupId>org.by1337.tcpapi.client</groupId>
            <artifactId>BTcpClient</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
        <!--Server-->
        <dependency>
            <groupId>org.by1337.tcpapi.server</groupId>
            <artifactId>BTcpServer</artifactId>
            <version>1.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

## Usage Examples

### Server

```java
ServerChannelStream stream = ServerManager.getChannelStreamManager().registerChannelStream(new SpacedNameKey("myPlugin:channel"), MyChannelListenerImpl);
for (Client client : stream.getAllClients()) {
    stream.sendTo(client, new PacketPingRequest());
}
```

### Client

```java
ClientChannelStream stream = Manager.getChannelStreamManager().registerChannelStream(new SpacedNameKey("myPlugin:channel"), MyChannelStreamPacketReaderImpl);
stream.register().lock(10_000);
if (stream.getStatus() != ChannelStatus.OPENED){
    throw new IllegalStateException("Failed to establish connection with the TCP server!");
}
stream.write(new PacketPingRequest());
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.