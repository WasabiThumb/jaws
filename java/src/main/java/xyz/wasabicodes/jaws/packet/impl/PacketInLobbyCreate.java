package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.util.FriendlyName;

import java.nio.charset.StandardCharsets;

public class PacketInLobbyCreate extends PacketIn {

    public int transaction = 0;
    public String name = "New Lobby"; // Length: 1 - 32
    public PacketInLobbyCreate() {
        super(5);
    }

    @Override
    public int getElementCount() {
        return 3;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        int transaction = dat.readUInt8();
        int len = (dat.readUInt8() & 0x1F) + 1;
        byte[] bytes = dat.readRemainingUpTo((len << 2) - len);
        if (!FriendlyName.isLegal(bytes)) throw new IllegalArgumentException("Illegal lobby name: " + new String(bytes, StandardCharsets.UTF_8));

        this.transaction = transaction;
        this.name = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void write(PacketData dat) {
        dat.writeUInt8(this.transaction);
        dat.writeUInt8((this.name.length() - 1) & 0x1F);
        byte[] bytes = this.name.getBytes(StandardCharsets.UTF_8);
        dat.writeLiteral(bytes);
    }

}
