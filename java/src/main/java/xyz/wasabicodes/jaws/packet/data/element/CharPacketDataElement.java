package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record CharPacketDataElement(char value) implements PacketDataElement {

    @Override
    public int size() {
        return Character.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putChar(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        dos.writeChar(this.value);
    }

}
