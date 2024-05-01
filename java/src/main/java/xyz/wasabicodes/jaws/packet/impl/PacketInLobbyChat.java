package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatMessage;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatSender;

import java.util.UUID;

public class PacketInLobbyChat extends PacketIn {

    /**
     * After {@link #read(PacketData)} the message stored here will NOT HAVE A SENDER SET!
     * It is up to the code that handles this packet to call {@link ChatMessage#setSender(ChatSender)}.
     * It should also check {@link ChatMessage#isLikelyNotLongerThan(int)} if a synthetic message length limit
     * is in place.
     */
    public ChatMessage message = ChatMessage.PLACEHOLDER;
    public PacketInLobbyChat() {
        super(10);
    }

    @Override
    public int getElementCount() {
        return 5;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        int len = dat.readUInt8();
        boolean fetchId = (len & 128) != 0;
        if (fetchId) len &= 127;
        if (len == 127) len = dat.readUInt16();

        long[] bits = new long[2];
        if (fetchId) {
            bits[0] = dat.readInt64();
            bits[1] = dat.readInt64();
            len += 41;
        } else {
            len++;
        }

        byte[] data = dat.readRemainingUpTo(len);
        if (fetchId) {
            this.message = new ChatMessage.Whisper(new UUID(bits[0], bits[1]), data);
        } else {
            this.message = new ChatMessage.Basic(data);
        }
    }

    @Override
    public void write(PacketData dat) {
        if (this.message.getType() == ChatMessage.Type.SYSTEM)
            throw new IllegalStateException("Cannot write a system message (" + this.message + ")");

        final byte[] raw = this.message.getRawData();
        int len = raw.length;
        if (len == 0) throw new IllegalStateException("Message is empty");
        boolean writeId = this.message.getType() == ChatMessage.Type.WHISPER;
        if (writeId) {
            len = Math.max(len - 41, 0);
        } else {
            len--;
        }
        if (len > 65535) throw new IllegalStateException("Message is longer than 65536 bytes");

        boolean writeFull = len > 126;
        int lenLo = writeFull ? 127 : len;
        if (writeId) lenLo |= 128;
        dat.writeUInt8(lenLo);
        if (writeFull) dat.writeUInt16(len);
        if (writeId) {
            ChatMessage.Whisper w = (ChatMessage.Whisper) this.message;
            dat.writeInt64(w.getReceiverID().getMostSignificantBits());
            dat.writeInt64(w.getReceiverID().getLeastSignificantBits());
        }
        dat.writeLiteral(raw);
    }

}
