package xyz.wasabicodes.jaws.packet.data.element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface PacketDataElement {

    int size();

    void write(ByteBuffer buffer);

    void writeBE(DataOutputStream dos) throws IOException;

    default void write(DataOutputStream dos, ByteOrder bo) throws IOException {
        if (bo == ByteOrder.BIG_ENDIAN) {
            this.writeBE(dos);
        } else {
            ByteBuffer bb = ByteBuffer.allocate(this.size());
            bb.order(bo);
            this.write(bb);
            dos.write(bb.array());
        }
    }

}
