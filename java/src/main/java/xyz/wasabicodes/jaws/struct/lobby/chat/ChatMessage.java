package xyz.wasabicodes.jaws.struct.lobby.chat;

import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.struct.User;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public sealed interface ChatMessage {

    ChatMessage PLACEHOLDER = new System("System Message");

    //

    ChatSender getSender();

    default UUID getSenderID() {
        ChatSender sender = this.getSender();
        if (sender.isUser()) return sender.asUser().getIdentifier();
        return JawsServer.SYSTEM_UUID;
    }

    byte[] getRawData();

    String getMessage() throws UnsupportedOperationException;

    String getMessage(EncryptMethod method, EncryptKey receiverKey);

    Type getType();

    default boolean isWhisper() {
        return this.getType() == Type.WHISPER;
    }

    //

    enum Type {
        SYSTEM,
        BASIC,
        WHISPER
    }

    //

    final class System implements ChatMessage {

        private final String message;
        public System(String message) {
            this.message = message;
        }

        @Override
        public ChatSender getSender() {
            return JawsServer.RUNNING_INSTANCE;
        }

        @Override
        public byte[] getRawData() {
            return this.message.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String getMessage() {
            return this.message;
        }

        @Override
        public String getMessage(EncryptMethod method, EncryptKey receiverKey) {
            return this.message;
        }

        @Override
        public Type getType() {
            return Type.SYSTEM;
        }

    }

    //

    final class Basic implements ChatMessage {

        private final User sender;
        private final byte[] message;
        public Basic(User sender, byte[] message) {
            this.sender = sender;
            this.message = message;
        }

        @Override
        public User getSender() {
            return this.sender;
        }

        @Override
        public byte[] getRawData() {
            return this.message;
        }

        @Override
        public String getMessage() {
            return new String(this.message);
        }

        @Override
        public String getMessage(EncryptMethod method, EncryptKey receiverKey) {
            return this.getMessage();
        }

        @Override
        public Type getType() {
            return Type.BASIC;
        }

    }

    //

    final class Whisper implements ChatMessage {

        private final User sender;
        private final UUID receiverID;
        private final byte[] encryptedMessage;
        public Whisper(User sender, UUID receiverID, byte[] encryptedMessage) {
            this.sender = sender;
            this.receiverID = receiverID;
            this.encryptedMessage = encryptedMessage;
        }

        @Override
        public User getSender() {
            return this.sender;
        }

        public UUID getReceiverID() {
            return this.receiverID;
        }

        @Override
        public byte[] getRawData() {
            return this.encryptedMessage;
        }

        @Override
        public String getMessage() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Cannot access whisper message without encryption key");
        }

        @Override
        public String getMessage(EncryptMethod method, EncryptKey receiverKey) {
            byte[] decrypted;
            try {
                decrypted = method.decrypt(this.encryptedMessage, this.sender.getKey(), receiverKey);
            } catch (IllegalArgumentException e) {
                return null;
            }
            return new String(decrypted, StandardCharsets.UTF_8);
        }

        @Override
        public Type getType() {
            return Type.WHISPER;
        }

    }

}
