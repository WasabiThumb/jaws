package xyz.wasabicodes.jaws.struct;

import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatSender;

import java.util.UUID;

public interface User extends ChatSender {

    UUID getIdentifier();

    String getName();

    EncryptKey getKey();

    @Override
    default boolean isUser() {
        return true;
    }

    @Override
    default User asUser() {
        return this;
    }

}
