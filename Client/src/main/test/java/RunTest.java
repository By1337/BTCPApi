import org.by1337.tcpapi.api.packet.impl.DisconnectPacket;
import org.by1337.tcpapi.client.network.Connection;
import org.junit.Test;

import java.util.logging.Logger;

public class RunTest {
    @Test
    public void run(){
        if (true){
            return; // nop test
        }
        Connection connection = new Connection("localhost", 8081, "id", "password", Logger.getLogger("test"));

        connection.start(true);

        connection.authWait();

        connection.sendPacket(new DisconnectPacket("123"));

        while (true) {
        }
    }
}
