package xyz.wasabicodes.jaws.packet;

import xyz.wasabicodes.jaws.packet.data.PacketData;

public abstract class Packet {

    protected final PacketIdentifier id;
    public Packet(PacketIdentifier id) {
        this.id = id;
    }

    public Packet(int id) {
        this(PacketIdentifier.from((byte) id));
    }

    //

    public final PacketIdentifier getIdentifier() {
        return this.id;
    }

    public abstract PacketDirection getDirection();

    public abstract int getElementCount();

    public abstract void read(PacketData dat) throws IllegalArgumentException;

    public abstract void write(PacketData dat);

    public boolean transmitPlain() {
        return false;
    }

}
