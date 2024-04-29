package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;

import java.util.UUID;

public class PacketOutSessionStart extends PacketOut {

    public UUID identifier;
    public PacketOutSessionStart() {
        super(2);
    }

    @Override
    public int getElementCount() {
        return 2;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        long mostSig = dat.readInt64();
        long leastSig = dat.readInt64();
        this.identifier = new UUID(mostSig, leastSig);
    }

    @Override
    public void write(PacketData dat) {
        dat.writeInt64(this.identifier.getMostSignificantBits());
        dat.writeInt64(this.identifier.getLeastSignificantBits());
    }

    @Override
    public boolean transmitPlain() {
        return true;
    }

}
