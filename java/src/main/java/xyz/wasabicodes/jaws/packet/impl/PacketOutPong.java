package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;

public class PacketOutPong extends PacketOut {

    public final byte[] data = new byte[512];
    public PacketOutPong() {
        super(4);
    }

    @Override
    public int getElementCount() {
        return 1;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        final byte[] value = dat.readLiteral(512);
        System.arraycopy(value, 0, this.data, 0, 512);
    }

    @Override
    public void write(PacketData dat) {
        dat.writeLiteral(this.data);
    }

}
