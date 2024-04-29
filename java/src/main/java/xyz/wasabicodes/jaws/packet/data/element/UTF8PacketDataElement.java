package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class UTF8PacketDataElement implements PacketDataElement {

    private final byte[] value;
    public UTF8PacketDataElement(String value) {
        this.value = value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int size() {
        return this.value.length + 1 + (this.value.length >= 0xFF ? 4 : 0);
    }

    @Override
    public void write(ByteBuffer buffer) {
        if (this.value.length >= 0xFF) {
            buffer.put((byte) 0xFF);
            buffer.putInt(this.value.length);
        } else {
            buffer.put((byte) this.value.length);
        }
        buffer.put(this.value);
    }

    @Override
    public void writeBE(DataOutputStream dos) throws IOException {
        if (this.value.length >= 0xFF) {
            dos.writeByte(0xFF);
            dos.writeInt(this.value.length);
        } else {
            dos.writeByte(this.value.length);
        }
        dos.write(this.value);
    }

    @Override
    public void write(DataOutputStream dos, ByteOrder bo) throws IOException {
        if (this.value.length < 0xFF) {
            dos.writeByte(this.value.length);
            dos.write(this.value);
        } else if (bo == ByteOrder.BIG_ENDIAN) {
            dos.writeByte(0xFF);
            dos.writeInt(this.value.length);
            dos.write(this.value);
        } else {
            ByteBuffer buf = ByteBuffer.allocate(this.value.length + 5);
            buf.order(bo);
            buf.put((byte) 0xFF);
            buf.putInt(this.value.length);
            buf.put(this.value);
            dos.write(buf.array());
        }
    }

}
