package xyz.wasabicodes.jaws.server.struct;

import xyz.wasabicodes.jaws.packet.impl.PacketOutLobbyData;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.lobby.Lobby;
import xyz.wasabicodes.jaws.struct.lobby.LobbyCode;

import java.util.*;

public class ServerLobby implements Lobby {

    private final JawsServer server;
    private final int id;
    private String name;
    private final Set<ServerUser> users = Collections.synchronizedSet(new HashSet<>());
    private ServerUser owner = null;
    private boolean publicMatchmaking = false;
    public ServerLobby(JawsServer server, int id, String name) {
        this.server = server;
        this.id = id;
        this.name = name;
    }

    public JawsServer getServer() {
        return this.server;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCode() {
        return LobbyCode.fromInt(this.id);
    }

    public int getIdentifier() {
        return this.id;
    }

    @Override
    public Collection<User> getUsers() {
        return Collections.unmodifiableSet(this.users);
    }

    @Override
    public User getUser(UUID id) {
        for (User u : this.users) {
            if (u.getIdentifier().equals(id)) return u;
        }
        return null;
    }

    @Override
    public ServerUser getOwner() {
        return this.owner;
    }

    @Override
    public boolean isPublic() {
        return this.publicMatchmaking;
    }

    @Override
    public void setPublic(boolean pub) {
        this.publicMatchmaking = pub;
    }

    public synchronized void setOwner(ServerUser su) {
        if (Objects.equals(su, this.owner)) return;
        if (su != null) {
            addUser(su);
            this.owner = su;
        }
    }

    public synchronized boolean addUser(ServerUser su) {
        boolean ret = false;
        if (su != null) {
            if (su.lobby != null && su.lobby != this) su.lobby.removeUser(su);
            ret = this.users.add(su);
            su.lobby = this;
            if (this.owner == null) this.owner = su;
        }
        return ret;
    }

    public synchronized boolean removeUser(ServerUser su) {
        if (su == null) return false;
        if (this.users.remove(su)) {
            su.lobby = null;
            if (Objects.equals(su, this.owner)) {
                this.owner = this.users.isEmpty() ? null : this.users.iterator().next();
            }
            return true;
        }
        return false;
    }

    public void broadcastData(int transaction) {
        PacketOutLobbyData dat = new PacketOutLobbyData();
        dat.transaction = transaction;
        dat.lobbyCode = this.id;
        dat.lobbyName = this.name;
        dat.isPublic = this.publicMatchmaking;

        ServerUser[] users = this.users.toArray(ServerUser[]::new);
        Arrays.sort(users, Comparator.naturalOrder());
        dat.userCount = users.length;
        dat.userIDs = new UUID[users.length];
        dat.userNames = new String[users.length];
        ServerUser su;
        for (int i=0; i < users.length; i++) {
            su = users[i];
            if (this.isOwner(su)) dat.ownerIndex = i;
            dat.userIDs[i] = su.getIdentifier();
            dat.userNames[i] = su.getName();
        }

        for (ServerUser receiver : users) receiver.sendPacket(dat);
    }

    //


    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ServerLobby other) {
            return this.id == other.id;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Lobby[code=" + this.getCode() + ", owner=" + this.owner.getName() + "]";
    }

}
