package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record Int16PacketDataElement(short value) implements PacketDataElement {

    @Override
    public int size() {
        return Short.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putShort(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeShort(this.value);
    }

}
