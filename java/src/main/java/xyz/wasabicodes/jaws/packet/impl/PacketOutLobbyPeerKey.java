package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.server.JawsServer;

import java.util.UUID;

public class PacketOutLobbyPeerKey extends PacketOut {

    public int transaction = 0;
    public UUID peerID = JawsServer.SYSTEM_UUID;
    public byte[] keyData;
    public PacketOutLobbyPeerKey() {
        super(9);
    }

    @Override
    public int getElementCount() {
        return 4;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        int transaction = dat.readUInt8();
        long mostSig = dat.readInt64();
        long leastSig = dat.readInt64();
        byte[] keyData = dat.readRemaining();
        this.transaction = transaction;
        this.peerID = new UUID(mostSig, leastSig);
        this.keyData = keyData;
    }

    @Override
    public void write(PacketData dat) {
        dat.writeUInt8(this.transaction);
        dat.writeInt64(this.peerID.getMostSignificantBits());
        dat.writeInt64(this.peerID.getLeastSignificantBits());
        dat.writeLiteral(this.keyData);
    }

}
