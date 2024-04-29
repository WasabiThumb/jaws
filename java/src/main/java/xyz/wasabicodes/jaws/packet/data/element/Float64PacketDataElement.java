package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record Float64PacketDataElement(double value) implements PacketDataElement {

    @Override
    public int size() {
        return Double.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putDouble(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeDouble(this.value);
    }

}
