package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record Int8PacketDataElement(byte value) implements PacketDataElement {

    @Override
    public int size() {
        return Byte.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeByte(this.value);
    }

    @Override
    public void write(DataOutputStream dos, ByteOrder bo) throws IOException {
        dos.writeByte(this.value);
    }

}
