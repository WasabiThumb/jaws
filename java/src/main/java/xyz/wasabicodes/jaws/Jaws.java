package xyz.wasabicodes.jaws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.config.ServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class Jaws {

    public static JawsServer createServer(ServerConfig cfg) {
        return new JawsServer(cfg);
    }

    public static JawsServer createServer() {
        return createServer(new ServerConfig());
    }

    private static Logger LOGGER = null;
    public static Logger getLogger() {
        if (LOGGER == null) LOGGER = LoggerFactory.getLogger(Jaws.class);
        return LOGGER;
    }

    private static byte PROTOCOL_VERSION = (byte) -1;
    private static final char[] PROTOCOL_VERSION_TXT = new char[] {
            'p', 'r', 'o', 't', 'o', 'c', 'o', 'l', '-', 'v', 'e', 'r', 's', 'i', 'o', 'n', '.', 't', 'x', 't'
    };
    public static byte getProtocolVersion() {
        if (PROTOCOL_VERSION == (byte) -1) {
            ClassLoader cl = Jaws.class.getClassLoader();

            final char[] packageName = Jaws.class.getPackageName().toCharArray();
            final char[] path = new char[packageName.length + PROTOCOL_VERSION_TXT.length + 1];
            int i = 0;
            for (; i < packageName.length; i++) {
                char c = packageName[i];
                path[i] = (c == '.' ? '/' : c);
            }
            path[i++] = '/';
            System.arraycopy(PROTOCOL_VERSION_TXT, 0, path, i, PROTOCOL_VERSION_TXT.length);

            try (InputStream is = cl.getResourceAsStream(new String(path))) {
                if (is == null) {
                    getLogger().warn("Failed to identify protocol version");
                    return PROTOCOL_VERSION = (byte) 0;
                }
                String conts = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                byte b = Byte.parseByte(conts);
                return PROTOCOL_VERSION = b;
            } catch (IOException | NumberFormatException e) {
                getLogger().warn("Failed to identify protocol version", e);
                return PROTOCOL_VERSION = (byte) 0;
            }
        }
        return PROTOCOL_VERSION;
    }

}
