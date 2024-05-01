package xyz.wasabicodes.jaws.server.struct;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.impl.PacketInLobbyChat;
import xyz.wasabicodes.jaws.packet.impl.PacketInLobbyRequestChatHistory;
import xyz.wasabicodes.jaws.packet.impl.PacketOutLobbyChat;
import xyz.wasabicodes.jaws.packet.impl.PacketOutLobbyChatHistory;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.config.ServerConfig;
import xyz.wasabicodes.jaws.server.config.ServerConfigKey;
import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatMessage;
import xyz.wasabicodes.jaws.struct.lobby.chat.LobbyChat;
import xyz.wasabicodes.jaws.util.CircularStack;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

public class ServerLobbyChat implements LobbyChat {

    private final ServerLobby lobby;
    private final int maxMessageLength;
    private final CircularStack<HistoryEntry> history;
    private int tokenHead = 0;
    private final StampedLock historyLock = new StampedLock();
    ServerLobbyChat(ServerLobby lobby) {
        this.lobby = lobby;
        ServerConfig cfg = lobby.getServer().getConfig();
        this.maxMessageLength = Math.max(Math.min(cfg.getInt(ServerConfigKey.CHAT_MAX_MESSAGE_LENGTH), 16384), 1);
        int historySize = Math.max(cfg.getInt(ServerConfigKey.CHAT_HISTORY_LENGTH), 1);
        this.history = new CircularStack<>(historySize);
    }

    public final JawsServer getServer() {
        return this.lobby.getServer();
    }

    @Override
    public int getMaxMessageLength() {
        return this.maxMessageLength;
    }

    @Override
    public ServerLobby getLobby() {
        return this.lobby;
    }

    @Override
    public List<ChatMessage> getMessageHistory() {
        long stamp = this.historyLock.readLock();
        try {
            ChatMessage[] buf = new ChatMessage[this.history.size()];
            for (int i=0; i < buf.length; i++) buf[i] = this.history.get(i).message;
            return Arrays.asList(buf);
        } finally {
            this.historyLock.unlock(stamp);
        }
    }

    private List<HistoryEntry> getAuthorizedMessageHistory(User user) {
        final UUID id = user.getIdentifier();
        HistoryEntry[] ret;
        int len = 0;

        long stamp = this.historyLock.readLock();
        try {
            ret = new HistoryEntry[this.history.size()];
            ChatMessage message;
            for (HistoryEntry entry : this.history) {
                message = entry.message;
                if (message.getType() == ChatMessage.Type.WHISPER) {
                    ChatMessage.Whisper whisper = (ChatMessage.Whisper) message;
                    if (!Objects.equals(whisper.getReceiverID(), id)) continue;
                }
                ret[len++] = entry;
            }
        } finally {
            this.historyLock.unlock(stamp);
        }

        return Arrays.asList(ret).subList(0, len);
    }

    private void submitMessage(Collection<User> audience, ChatMessage message, UUID exclude, boolean addToHistory) {
        final boolean hasExclusion = exclude != null;

        int token;
        long stamp = this.historyLock.writeLock();
        try {
            token = this.tokenHead++;
            if (addToHistory) this.history.add(new HistoryEntry(message, token));
        } finally {
            this.historyLock.unlock(stamp);
        }

        PacketOutLobbyChat packet = new PacketOutLobbyChat();
        packet.message = message;
        packet.senderID = message.getSenderID();
        packet.idempotency = token;
        for (User u : audience) {
            if (hasExclusion && exclude.equals(u.getIdentifier())) continue;
            ((ServerUser) u).sendPacket(packet);
        }
    }

    @Override
    public ChatMessage broadcast(String content) {
        ChatMessage.System cs = new ChatMessage.System(content);
        cs.setSender(this.lobby.getServer());
        this.submitMessage(this.lobby.getUsers(), cs, null, true);
        return cs;
    }

    @Override
    public ChatMessage whisper(User receiver, String content) {
        ChatMessage.System cs = new ChatMessage.System(content);
        cs.setSender(this.lobby.getServer());
        this.submitMessage(Collections.singleton(receiver), cs, null, false);
        return cs;
    }

    public void handlePacket(ServerUser sender, PacketIn packet) {
        if (packet instanceof PacketInLobbyChat pChat) {
            ChatMessage cm = pChat.message;
            cm.setSender(sender);
            if (!cm.isLikelyNotLongerThan(this.maxMessageLength)) return;
            if (cm.getType() == ChatMessage.Type.WHISPER) {
                ChatMessage.Whisper whisper = (ChatMessage.Whisper) cm;
                UUID receiver = whisper.getReceiverID();
                User dest = this.lobby.getUser(receiver);
                if (dest != null) this.submitMessage(Collections.singleton(dest), whisper, null, true);
            } else {
                this.submitMessage(this.lobby.getUsers(), cm, sender.getIdentifier(), true);
            }
        } else if (packet instanceof PacketInLobbyRequestChatHistory pHistory) {
            PacketOutLobbyChatHistory response = new PacketOutLobbyChatHistory();
            response.transaction = pHistory.transaction;

            final List<HistoryEntry> history = this.getAuthorizedMessageHistory(sender);
            final int size = history.size();
            response.messages = new ChatMessage[size];
            response.tokens = new int[size];
            HistoryEntry single;
            for (int i=0; i < size; i++) {
                single = history.get(i);
                response.messages[i] = single.message;
                response.tokens[i] = single.token;
            }

            sender.sendPacket(response);
        }
    }

    private record HistoryEntry(ChatMessage message, int token) { }

}
