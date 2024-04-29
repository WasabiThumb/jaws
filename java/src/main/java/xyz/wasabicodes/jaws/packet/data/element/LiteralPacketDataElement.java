package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record LiteralPacketDataElement(byte[] value) implements PacketDataElement {

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.write(this.value);
    }

    @Override
    public void write(DataOutputStream dos, ByteOrder bo) throws IOException {
        dos.write(this.value);
    }

}
