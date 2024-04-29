package xyz.wasabicodes.jaws.server.struct;

import org.java_websocket.WebSocket;
import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.struct.User;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.UUID;

public class ServerUser implements User, Comparable<ServerUser> {

    private final JawsServer server;
    private final WebSocket connection;
    private final UUID id;
    private final String name;
    private final EncryptKey key;
    private final ByteOrder order;
    protected ServerLobby lobby = null;
    public ServerUser(JawsServer server, WebSocket connection, UUID id, String name, EncryptKey key, ByteOrder order) {
        this.server = server;
        this.connection = connection;
        this.id = id;
        this.name = name;
        this.key = key;
        this.order = order;
    }

    public JawsServer getServer() {
        return this.server;
    }

    public WebSocket getConnection() {
        return this.connection;
    }

    public ServerLobby getLobby() {
        return this.lobby;
    }

    @Override
    public UUID getIdentifier() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public EncryptKey getKey() {
        return this.key;
    }

    public ByteOrder getByteOrder() {
        return this.order;
    }

    public boolean sendPacket(PacketOut packet) {
        return this.server.sendPacket(this, packet);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ServerUser other) {
            return Objects.equals(this.id, other.id);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(ServerUser serverUser) {
        int cmp = Long.compare(this.id.getMostSignificantBits(), serverUser.id.getMostSignificantBits());
        if (cmp == 0) cmp = Long.compare(this.id.getLeastSignificantBits(), serverUser.id.getLeastSignificantBits());
        return cmp;
    }

}
