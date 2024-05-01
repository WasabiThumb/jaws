package xyz.wasabicodes.jaws.server.facet;

import xyz.wasabicodes.jaws.packet.PacketIdentifier;
import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.impl.PacketInLobbyCreate;
import xyz.wasabicodes.jaws.packet.impl.PacketInLobbyJoin;
import xyz.wasabicodes.jaws.packet.impl.PacketInLobbyRequestPeerKey;
import xyz.wasabicodes.jaws.packet.impl.PacketOutLobbyPeerKey;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.config.ServerConfigKey;
import xyz.wasabicodes.jaws.server.struct.ServerLobby;
import xyz.wasabicodes.jaws.server.struct.ServerUser;
import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.util.AOInt2ObjectMap;
import xyz.wasabicodes.jaws.util.Optimus;

import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

public class LobbyServerFacet extends ServerFacetAdapter {

    private static final int ID_MASK = 0x3FFFFFFF;
    private final Optimus optimus = Optimus.generate(30);
    private int lobbyHead = 0;
    private final LobbyMap lobbyMap = new LobbyMap();
    private final StampedLock lobbyLock = new StampedLock();
    private JawsServer server;

    //

    @Override
    public void onInit(JawsServer ctx) {
        this.server = ctx;
    }

    public ServerLobby createLobby(String name, ServerUser owner) {
        long stamp = this.lobbyLock.writeLock();
        try {
            int id = (this.lobbyHead++) & ID_MASK;
            ServerLobby sl = new ServerLobby(this.server, this.optimus.encode(id), name);
            sl.setOwner(owner);
            if ((id & 15) == 15) this.lobbyMap.purge();
            this.lobbyMap.put(id, sl);
            return sl;
        } finally {
            this.lobbyLock.unlock(stamp);
        }
    }

    public void cleanupLobbyIfEmpty(ServerLobby sl) {
        if (sl == null) return;
        if (!sl.getUsers().isEmpty()) return;
        int id = this.optimus.decode(sl.getIdentifier());
        long stamp = this.lobbyLock.readLock();
        try {
            if (!Objects.equals(this.lobbyMap.get(id), sl)) return;
            stamp = this.lobbyLock.tryConvertToWriteLock(stamp);
            if (stamp != 0L) this.lobbyMap.remove(id);
        } finally {
            this.lobbyLock.unlock(stamp);
        }
    }

    @Override
    public void onMessage(JawsServer ctx, ServerUser user, PacketIn received) {
        final PacketIdentifier pid = received.getIdentifier();
        if (!pid.inRange(5, 13)) return; // FIXME: Magic numbers
        if (received instanceof PacketInLobbyCreate create) {
            ServerLobby sl = createLobby(create.name, user);
            sl.broadcastData(create.transaction);
        } else if (received instanceof PacketInLobbyJoin join) {
            int id = this.optimus.decode(join.lobbyCode);
            final ServerLobby old = user.getLobby();
            ServerLobby sl;
            long stamp = this.lobbyLock.readLock();
            try {
                sl = this.lobbyMap.get(id);
                if (sl == null) return;
                // TODO: Implement fail condition
                if (sl.addUser(user)) sl.broadcastData(join.transaction);
            } finally {
                this.lobbyLock.unlock(stamp);
            }
            if (ctx.getConfig().getBoolean(ServerConfigKey.LOBBY_JOIN_MESSAGES)) {
                sl.getChat().broadcast("* " + user.getName() + " has joined the lobby");
            }
            this.cleanupLobbyIfEmpty(old);
        } else if (received instanceof PacketInLobbyRequestPeerKey request) {
            ServerLobby sl = user.getLobby();
            if (sl == null) return;
            User subject = sl.getUser(request.peerID);
            if (subject == null) return;

            PacketOutLobbyPeerKey response = new PacketOutLobbyPeerKey();
            response.transaction = request.transaction;
            response.peerID = request.peerID;
            response.keyData = subject.getKey().export();
            user.sendPacket(response);
        } else {
            ServerLobby sl = user.getLobby();
            if (sl == null) return;
            sl.getChat().handlePacket(user, received);
        }
    }

    @Override
    public void onDisconnect(JawsServer ctx, ServerUser user) {
        ServerLobby sl = user.getLobby();
        if (sl == null) return;
        if (sl.removeUser(user)) {
            sl.broadcastData(-1);
            if (ctx.getConfig().getBoolean(ServerConfigKey.LOBBY_LEAVE_MESSAGES)) {
                sl.getChat().broadcast("* " + user.getName() + " has left the lobby");
            }
        }
        this.cleanupLobbyIfEmpty(sl);
    }

    //

    static class LobbyMap extends AOInt2ObjectMap<ServerLobby> {

        public LobbyMap() {
            super(ServerLobby.class);
        }

        void purge() {
            this.removeIf(this::purge0);
        }

        private boolean purge0(ServerLobby item) {
            return item.getUsers().isEmpty();
        }

    }

}
