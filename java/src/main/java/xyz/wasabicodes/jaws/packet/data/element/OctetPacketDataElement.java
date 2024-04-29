package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OctetPacketDataElement implements PacketDataElement {

    private int flag;
    private int count;
    public OctetPacketDataElement(boolean initial) {
        this.flag = initial ? 1 : 0;
        this.count = 1;
    }

    public void put(boolean value) throws IllegalStateException {
        if (this.count >= 8) throw new IllegalStateException("Octet is full");
        if (value) this.flag |= (1 << this.count);
        this.count++;
    }

    public int numOctets() {
        return this.count;
    }

    @Override
    public int size() {
        return Byte.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put((byte) this.flag);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeByte(this.flag);
    }

    @Override
    public void write(DataOutputStream dos, ByteOrder bo) throws IOException {
        dos.writeByte(this.flag);
    }

}
