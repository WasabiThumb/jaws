package xyz.wasabicodes.jaws.struct.lobby.chat;

import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.lobby.Lobby;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LobbyChat {

    Lobby getLobby();

    /**
     * This will not cause a blocking operation on the client. It will simply give a copy of the latest
     * cached values.
     */
    List<ChatMessage> getMessageHistory();

    ChatMessage broadcast(String content);

    ChatMessage whisper(User receiver, String content);

    int getMaxMessageLength();

    default CompletableFuture<Void> refresh() {
        return CompletableFuture.completedFuture(null);
    }

}
