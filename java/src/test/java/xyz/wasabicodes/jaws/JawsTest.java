package xyz.wasabicodes.jaws;

import org.junit.jupiter.api.Test;
import xyz.wasabicodes.jaws.compress.CompressMethod;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.config.ServerConfig;
import xyz.wasabicodes.jaws.server.config.ServerConfigKey;
import xyz.wasabicodes.jaws.server.facet.LobbyServerFacet;
import xyz.wasabicodes.jaws.server.facet.PingServerFacet;

import java.util.concurrent.TimeUnit;

class JawsTest {

    @Test
    void createServer() {
        JawsServer srv = Jaws.createServer(ServerConfig.builder()
                .set(ServerConfigKey.PORT, 34345)
                .set(ServerConfigKey.ENCRYPTION, EncryptMethod.NACL)
                .set(ServerConfigKey.COMPRESSION, CompressMethod.NONE)
                .build()
        );
        srv.registerFacets(PingServerFacet.class, LobbyServerFacet.class);
        srv.start();

        Jaws.getLogger().info("Waiting 200 seconds...");
        try {
            TimeUnit.SECONDS.sleep(200L);
            srv.stop();
        } catch (InterruptedException ignored) { }
    }

}