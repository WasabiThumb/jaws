package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.struct.lobby.chat.ChatMessage;

import java.util.UUID;

public class PacketOutLobbyChatHistory extends PacketOut {

    public int transaction;
    public int[] tokens = new int[0];
    /**
     * Only used on client
     */
    public UUID[] messageSenders = new UUID[0];
    public ChatMessage[] messages = new ChatMessage[0];
    public PacketOutLobbyChatHistory() {
        super(13);
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        this.transaction = dat.readInt32();

        final int count = dat.readUInt16();
        this.tokens = new int[count];
        this.messageSenders = new UUID[count];
        this.messages = new ChatMessage[count];

        PacketOutLobbyChat single = new PacketOutLobbyChat();
        for (int i=0; i < count; i++) {
            single.read(dat);
            this.tokens[i] = single.idempotency;
            this.messageSenders[i] = single.senderID;
            this.messages[i] = single.message;
        }
    }

    @Override
    public void write(PacketData dat) {
        dat.writeInt32(this.transaction);
        dat.writeUInt16(this.messages.length);

        PacketOutLobbyChat single = new PacketOutLobbyChat();
        for (int i=0; i < this.messages.length; i++) {
            single.message = this.messages[i];
            single.idempotency = this.tokens[i];
            single.write(dat);
        }
    }

}
