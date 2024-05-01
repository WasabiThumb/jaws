package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatMessage;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatSender;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketOutLobbyChat extends PacketOut {

    private static final long CURSED_FLAG = 0x4000000000000000L;
    /**
     * Only used on client, otherwise the sender ID stored in {@link #message} is used
     */
    public UUID senderID = JawsServer.SYSTEM_UUID;
    /**
     * Reference {@link #senderID} to inform what to pass to {@link ChatMessage#setSender(ChatSender)}
     * after {@link #read(PacketData)}
     */
    public ChatMessage message = ChatMessage.PLACEHOLDER;
    public int idempotency = 0;
    public PacketOutLobbyChat() {
        super(11);
    }

    @Override
    public int getElementCount() {
        return 7;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        int seq = dat.readInt32();

        long mostSig = dat.readInt64();
        ChatMessage.Type type;
        long[] receiver = new long[2];

        if (mostSig == 0L) {
            type = ChatMessage.Type.SYSTEM;
            this.senderID = JawsServer.SYSTEM_UUID;
        } else {
            long leastSig = dat.readInt64();
            if ((leastSig & CURSED_FLAG) != 0) {
                receiver[0] = dat.readInt64();
                receiver[1] = dat.readInt64();
                type = ChatMessage.Type.WHISPER;
                leastSig ^= CURSED_FLAG;
            } else {
                type = ChatMessage.Type.BASIC;
            }
            this.senderID = new UUID(mostSig, leastSig);
        }

        int len = dat.readUInt16();
        byte[] raw = dat.readLiteral(len);
        this.message = switch (type) {
            case BASIC -> new ChatMessage.Basic(raw);
            case WHISPER -> new ChatMessage.Whisper(new UUID(receiver[0], receiver[1]), raw);
            case SYSTEM -> new ChatMessage.System(new String(raw, StandardCharsets.UTF_8));
        };

        this.idempotency = seq;
    }

    @Override
    public void write(PacketData dat) {
        dat.writeInt32(this.idempotency);
        UUID senderID = this.message.getSenderID();
        dat.writeInt64(senderID.getMostSignificantBits());

        if (this.message.getType() != ChatMessage.Type.SYSTEM) {
            long leastSig = senderID.getLeastSignificantBits();
            if (this.message.getType() == ChatMessage.Type.WHISPER) {
                ChatMessage.Whisper w = (ChatMessage.Whisper) this.message;
                dat.writeInt64(leastSig | CURSED_FLAG);
                dat.writeInt64(w.getReceiverID().getMostSignificantBits());
                dat.writeInt64(w.getReceiverID().getLeastSignificantBits());
            } else {
                dat.writeInt64(leastSig);
            }
        }

        byte[] data = this.message.getRawData();
        dat.writeUInt16(data.length);
        dat.writeLiteral(data);
    }

}
