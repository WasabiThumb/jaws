package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record Float32PacketDataElement(float value) implements PacketDataElement {

    @Override
    public int size() {
        return Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putFloat(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeFloat(this.value);
    }

}
