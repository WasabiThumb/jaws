package xyz.wasabicodes.jaws.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import xyz.wasabicodes.jaws.Jaws;
import xyz.wasabicodes.jaws.compress.CompressMethod;
import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.packet.Packet;
import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.PacketSerializer;
import xyz.wasabicodes.jaws.packet.impl.*;
import xyz.wasabicodes.jaws.server.config.ServerConfig;
import xyz.wasabicodes.jaws.server.config.ServerConfigKey;
import xyz.wasabicodes.jaws.server.struct.ServerUser;
import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.facet.Facet;
import xyz.wasabicodes.jaws.struct.facet.FacetContext;
import xyz.wasabicodes.jaws.struct.facet.FacetContextImpl;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatSender;
import xyz.wasabicodes.jaws.util.DistributedConcurrentHashMap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class JawsServer extends WebSocketServer implements FacetContext<JawsServer>, ChatSender {

    public static final UUID SYSTEM_UUID = new UUID(0L, 0L);
    public static JawsServer RUNNING_INSTANCE = null;
    public static ServerConfig activeConfig() {
        return Objects.requireNonNull(RUNNING_INSTANCE).getConfig();
    }

    private final ServerConfig cfg;
    private final EncryptKey key;
    private final DistributedConcurrentHashMap<WebSocket, ServerUser> userMap = new DistributedConcurrentHashMap<>(4); // At a depth of 4, there is a 93.75% chance that concurrent writes do not collide.
    private final FacetContextImpl<JawsServer> facets = new FacetContextImpl<>();
    public JawsServer(ServerConfig cfg) {
        super(new InetSocketAddress(cfg.getInt(ServerConfigKey.PORT)));
        this.cfg = cfg;
        this.key = cfg.getObject(ServerConfigKey.ENCRYPTION).generateLocalKey();
    }

    public final ServerConfig getConfig() {
        return this.cfg;
    }
    @Override
    public void run() {
        this.facets.lock(this);
        RUNNING_INSTANCE = this;
        super.run();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Jaws.getLogger().info("New connection from " + conn);
        PacketOutPreflight preflight = new PacketOutPreflight();
        preflight.protocolVersion = Jaws.getProtocolVersion();
        preflight.compression = this.cfg.getObject(ServerConfigKey.COMPRESSION);
        preflight.encryption = this.cfg.getObject(ServerConfigKey.ENCRYPTION);
        preflight.serverKey = this.key;
        conn.send(PacketSerializer.serialize(preflight, ByteOrder.BIG_ENDIAN));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        ServerUser su = this.userMap.remove(conn);
        if (su != null) {
            Jaws.getLogger().info(su.getName() + (remote ? " disconnected" : " lost connection"));
            this.facets.fireDisconnect(this, su);
        } else {
            Jaws.getLogger().info("Unauthenticated User (" + conn + ")" + (remote ? " disconnected" : " lost connection"));
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        this.onMessage(conn, ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        ServerUser user = this.userMap.get(conn);
        ByteOrder order = ByteOrder.BIG_ENDIAN;

        boolean loggedIn = user != null;
        if (loggedIn) {
            order = user.getByteOrder();
            try {
                message = unwrap(message, user);
            } catch (Exception e) {
                Jaws.getLogger().warn("Unable to parse packet from " + conn);
                return;
            }
        }

        Packet p;
        try {
            p = PacketSerializer.deserialize(message, order);
        } catch (Exception e) {
            Jaws.getLogger().warn("Unable to parse packet from " + conn);
            return;
        }

        if (loggedIn) {
            if (p instanceof PacketIn pi) {
                this.facets.fireMessage(this, user, pi);
            } else {
                Jaws.getLogger().warn("User (" + user + ") sent packet (" + p + ") that is not a PacketIn");
            }
            return;
        }

        if (p instanceof PacketInLogin pl) {
            EncryptMethod method = this.cfg.getObject(ServerConfigKey.ENCRYPTION);
            EncryptKey key;
            try {
                key = method.importRemoteKey(pl.keyData);
            } catch (IllegalArgumentException ex) {
                Jaws.getLogger().warn("Invalid key in login packet from " + conn, ex);
                return;
            }

            UUID u;
            ServerUser su;
            do {
                u = UUID.randomUUID();
                su = new ServerUser(this, conn, u, pl.name, key, pl.order);
            } while (this.userMap.containsValue(su));

            PacketOutSessionStart response = new PacketOutSessionStart();
            response.identifier = u;
            conn.send(PacketSerializer.serialize(response, pl.order));
            this.userMap.put(conn, su);
            Jaws.getLogger().info(pl.name + " (" + conn + ") logged in");
            this.facets.fireConnect(this, su);
        }
    }

    @Override
    public void onStart() {
        Jaws.getLogger().info("Server opened at " + this.getAddress().toString());
        this.facets.fireStart(this);
    }

    @Override
    public void stop(int timeout, String closeMessage) throws InterruptedException {
        this.facets.fireEnd(this);
        super.stop(timeout, closeMessage);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Jaws.getLogger().error("Server failed to open", ex);
    }

    public boolean sendPacket(ServerUser su, PacketOut packet) {
        WebSocket conn = su.getConnection();
        if (!conn.isOpen()) {
            this.userMap.remove(su.getConnection());
            return false;
        }

        byte[] bytes = PacketSerializer.serialize(packet, su.getByteOrder());

        EncryptMethod em = this.cfg.getObject(ServerConfigKey.ENCRYPTION);
        bytes = em.encrypt(bytes, this.key, su.getKey());

        CompressMethod cm = this.cfg.getObject(ServerConfigKey.COMPRESSION);
        bytes = cm.decompress(bytes);

        conn.send(bytes);
        return true;
    }

    //

    private ByteBuffer unwrap(ByteBuffer inBuffer, ServerUser su) {
        byte[] bytes;
        if (inBuffer.hasArray()) {
            bytes = inBuffer.array();
            int offset = inBuffer.arrayOffset();
            if (offset != 0) {
                int len = bytes.length - offset;
                byte[] cpy = new byte[len];
                System.arraycopy(bytes, offset, cpy, 0, len);
                bytes = cpy;
            }
        } else {
            bytes = new byte[inBuffer.limit()];
            inBuffer.get(0, bytes);
        }

        CompressMethod cm = this.cfg.getObject(ServerConfigKey.COMPRESSION);
        bytes = cm.decompress(bytes);

        EncryptMethod em = this.cfg.getObject(ServerConfigKey.ENCRYPTION);
        bytes = em.decrypt(bytes, su.getKey(), this.key);

        return ByteBuffer.wrap(bytes);
    }

    @SafeVarargs
    @Override
    public final void registerFacets(Facet<JawsServer>... facets) {
        this.facets.registerFacets(facets);
    }

    @SafeVarargs
    @Override
    public final void registerFacets(Class<? extends Facet<JawsServer>>... facetClasses) {
        this.facets.registerFacets(facetClasses);
    }

    @Override
    public <T extends Facet<JawsServer>> T getFacet(Class<T> clazz) {
        return this.facets.getFacet(clazz);
    }

    @Override
    public Collection<Facet<JawsServer>> getAllFacets() {
        return this.facets.getAllFacets();
    }

    @Override
    public String getName() {
        return "SYSTEM";
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public User asUser() {
        throw new ClassCastException("Server (SYSTEM) cannot be cast to User");
    }

}
