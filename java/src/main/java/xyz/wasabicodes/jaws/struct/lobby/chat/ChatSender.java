package xyz.wasabicodes.jaws.struct.lobby.chat;

import xyz.wasabicodes.jaws.struct.User;

public interface ChatSender {

    String getName();

    default boolean isUser() {
        return this instanceof User;
    }

    default boolean isSystem() {
        return false;
    }

    default User asUser() {
        return (User) this;
    }

}
