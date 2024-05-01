package xyz.wasabicodes.jaws.struct.lobby.chat;

import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.struct.User;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public sealed interface ChatMessage {

    ChatMessage PLACEHOLDER = new System("System Message");

    //

    ChatSender getSender();

    void setSender(ChatSender sender) throws IllegalStateException;

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

    boolean isLikelyNotLongerThan(int len);

    //

    enum Type {
        SYSTEM,
        BASIC,
        WHISPER
    }

    //

    final class System implements ChatMessage {

        private ChatSender sender = null;
        private final String message;
        public System(String message) {
            this.message = message;
        }

        @Override
        public ChatSender getSender() {
            return Objects.requireNonNull(this.sender);
        }

        @Override
        public void setSender(ChatSender sender) throws IllegalStateException {
            if (this.sender != null) throw new IllegalStateException("Cannot set message sender more than once");
            this.sender = sender;
        }

        @Override
        public UUID getSenderID() {
            return JawsServer.SYSTEM_UUID;
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

        @Override
        public boolean isLikelyNotLongerThan(int len) {
            return len >= this.message.length();
        }
    }

    //

    final class Basic implements ChatMessage {

        private User sender = null;
        private final byte[] message;
        public Basic(byte[] message) {
            this.message = message;
        }

        @Override
        public User getSender() {
            return Objects.requireNonNull(this.sender);
        }

        @Override
        public void setSender(ChatSender sender) throws IllegalStateException {
            if (this.sender != null) throw new IllegalStateException("Cannot set message sender more than once");
            if (sender instanceof User u) {
                this.sender = u;
            } else {
                throw new IllegalArgumentException("Cannot set message sender to a non-user: " + sender);
            }
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

        @Override
        public boolean isLikelyNotLongerThan(int len) {
            return len >= this.getMessage().length();
        }

    }

    //

    final class Whisper implements ChatMessage {

        private User sender = null;
        private final UUID receiverID;
        private final byte[] encryptedMessage;
        public Whisper(UUID receiverID, byte[] encryptedMessage) {
            this.receiverID = receiverID;
            this.encryptedMessage = encryptedMessage;
        }

        @Override
        public User getSender() {
            return Objects.requireNonNull(this.sender);
        }

        @Override
        public void setSender(ChatSender sender) throws IllegalStateException {
            if (this.sender != null) throw new IllegalStateException("Cannot set message sender more than once");
            if (sender instanceof User u) {
                this.sender = u;
            } else {
                throw new IllegalArgumentException("Cannot set message sender to a non-user: " + sender);
            }
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

        @Override
        public boolean isLikelyNotLongerThan(int len) {
            return ((len << 2) + 40) >= this.encryptedMessage.length;
        }

    }

}
