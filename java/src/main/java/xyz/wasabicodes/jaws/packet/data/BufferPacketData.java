package xyz.wasabicodes.jaws.packet.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BufferPacketData implements PacketData {

    private final ByteBuffer buf;
    public BufferPacketData(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int size() {
        return buf.limit();
    }

    @Override
    public ByteOrder byteOrder() {
        return buf.order();
    }

    @Override
    public void byteOrder(ByteOrder byteOrder) {
        buf.order(byteOrder);
    }

    @Override
    public byte[] toBytes() {
        if (buf.hasArray()) {
            byte[] arr = buf.array();
            int off = buf.arrayOffset();
            if (off == 0) return arr;
            int len = arr.length;
            byte[] ret = new byte[len - off];
            System.arraycopy(arr, off, ret, 0, ret.length);
            return ret;
        } else {
            buf.position(0);
            byte[] ret = new byte[buf.limit()];
            buf.get(ret);
            return ret;
        }
    }

    @Override
    public void toBytes(byte[] buf, int off) {
        this.buf.get(0, buf, off, Math.min(buf.length - off, this.buf.limit()));
    }

    //


    @Override
    public byte readInt8() {
        this.boolReadRemaining = 0;
        return buf.get();
    }

    @Override
    public short readInt16() {
        this.boolReadRemaining = 0;
        return buf.getShort();
    }

    @Override
    public int readInt32() {
        this.boolReadRemaining = 0;
        return buf.getInt();
    }

    @Override
    public long readInt64() {
        this.boolReadRemaining = 0;
        return buf.getLong();
    }

    @Override
    public float readFloat32() {
        this.boolReadRemaining = 0;
        return buf.getFloat();
    }

    @Override
    public double readFloat64() {
        this.boolReadRemaining = 0;
        return buf.getDouble();
    }

    @Override
    public boolean readBoolean() {
        if (this.boolReadRemaining == 0) {
            this.boolRead = this.readUInt8();
            this.boolReadRemaining = 8;
        }
        boolean ret = (this.boolRead & 1) == 1;
        this.boolRead >>= 1;
        this.boolReadRemaining--;
        return ret;
    }

    @Override
    public char readChar() {
        this.boolReadRemaining = 0;
        return buf.getChar();
    }

    @Override
    public String readUTF8() {
        this.boolReadRemaining = 0;
        int len = this.readUInt8();
        if (len == 255) len = this.readInt32();
        return new String(this.readLiteral(len), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readLiteral(int len) throws UnsupportedOperationException {
        byte[] b = new byte[len];
        buf.get(b);
        return b;
    }

    @Override
    public byte[] readRemaining() throws UnsupportedOperationException {
        final int len = buf.remaining();
        byte[] b = new byte[len];
        if (len > 0) buf.get(b);
        return b;
    }

    @Override
    public byte[] readRemainingUpTo(int len) throws UnsupportedOperationException {
        len = Math.min(len, buf.remaining());
        byte[] b = new byte[len];
        if (len > 0) buf.get(b);
        return b;
    }

    // Boolean reads
    private int boolReadRemaining = 0;
    private int boolRead = 0;
    
}
