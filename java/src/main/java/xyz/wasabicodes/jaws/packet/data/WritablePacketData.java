package xyz.wasabicodes.jaws.packet.data;

import xyz.wasabicodes.jaws.packet.data.element.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WritablePacketData implements PacketData {

    private final List<PacketDataElement> elements;
    private final boolean linked;
    private ByteOrder order = ByteOrder.BIG_ENDIAN;
    private int totalSize = 0;
    public WritablePacketData(int elementCapacity) {
        this.elements = new ArrayList<>(elementCapacity);
        this.linked = false;
    }

    public WritablePacketData() {
        this.elements = new LinkedList<>();
        this.linked = true;
    }

    private void putElement(PacketDataElement el) {
        this.elements.add(el);
        this.totalSize += el.size();
    }

    @Override
    public int size() {
        return this.totalSize;
    }

    @Override
    public ByteOrder byteOrder() {
        return this.order;
    }

    @Override
    public void byteOrder(ByteOrder byteOrder) {
        this.order = byteOrder;
    }

    @Override
    public void toBytes(byte[] bytes, int off) {
        if (off < 0 || off > bytes.length) throw new IndexOutOfBoundsException("Offset " + off + " out of bounds for length " + bytes.length);
        ByteBuffer buf = ByteBuffer.wrap(bytes, off, bytes.length - off);
        buf.order(this.order);
        for (PacketDataElement el : this.elements) el.write(buf);
    }

    @Override
    public void write(DataOutputStream dos) throws IOException {
        for (PacketDataElement el : this.elements) el.write(dos, this.order);
    }

    //


    @Override
    public void writeInt8(int int8) {
        this.putElement(new Int8PacketDataElement((byte) int8));
    }

    @Override
    public void writeInt16(int int16) {
        this.putElement(new Int16PacketDataElement((short) int16));
    }

    @Override
    public void writeInt32(int int32) {
        this.putElement(new Int32PacketDataElement(int32));
    }

    @Override
    public void writeInt64(long int64) {
        this.putElement(new Int64PacketDataElement(int64));
    }

    @Override
    public void writeFloat32(float float32) {
        this.putElement(new Float32PacketDataElement(float32));
    }

    @Override
    public void writeFloat64(double float64) {
        this.putElement(new Float64PacketDataElement(float64));
    }

    @Override
    public void writeBoolean(boolean bool) {
        PacketDataElement last = null;
        int s = 0;
        if (this.linked) {
            last = ((LinkedList<PacketDataElement>) this.elements).peekLast();
        } else if ((s = this.elements.size()) > 0) {
            last = this.elements.get(s - 1);
        }

        if (last instanceof OctetPacketDataElement oct && oct.numOctets() < 8) {
            oct.put(bool);
            if (this.linked) {
                ((LinkedList<PacketDataElement>) this.elements).removeLast();
                this.elements.add(oct);
            } else {
                this.elements.set(s - 1, oct);
            }
        } else {
            OctetPacketDataElement newOctet = new OctetPacketDataElement(bool);
            this.putElement(newOctet);
        }
    }

    @Override
    public void writeChar(char c) {
        this.putElement(new CharPacketDataElement(c));
    }

    @Override
    public void writeUTF8(String utf) {
        this.putElement(new UTF8PacketDataElement(utf));
    }

    @Override
    public void writeLiteral(byte[] literal) throws UnsupportedOperationException {
        this.putElement(new LiteralPacketDataElement(literal));
    }

}
