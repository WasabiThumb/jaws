package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record Int64PacketDataElement(long value) implements PacketDataElement {

    @Override
    public int size() {
        return Long.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putLong(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeLong(this.value);
    }

}
