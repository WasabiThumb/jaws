package xyz.wasabicodes.jaws.struct.lobby;

import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.lobby.chat.LobbyChat;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public interface Lobby {

    String getName();

    String getCode();

    Collection<User> getUsers();

    default User getUser(UUID id) {
        for (User u : this.getUsers()) {
            if (u.getIdentifier().equals(id)) return u;
        }
        return null;
    }

    User getOwner();

    LobbyChat getChat();

    default boolean isOwner(User u) {
        return Objects.equals(this.getOwner(), u);
    }

    boolean isPublic();

    void setPublic(boolean pub);

}
