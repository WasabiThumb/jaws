package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.data.PacketData;

public class PacketInLobbyRequestChatHistory extends PacketIn {

    public int transaction;
    public PacketInLobbyRequestChatHistory() {
        super(12);
    }

    @Override
    public int getElementCount() {
        return 1;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        this.transaction = dat.readInt32();
    }

    @Override
    public void write(PacketData dat) {
        dat.writeInt32(this.transaction);
    }

}
