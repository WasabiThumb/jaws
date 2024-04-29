package xyz.wasabicodes.jaws.packet.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface PacketData {

    static PacketData create() {
        return new WritablePacketData();
    }

    static PacketData create(int elementCapacity) {
        if (elementCapacity < 1) return create();
        return new WritablePacketData(elementCapacity);
    }

    static BufferPacketData of(ByteBuffer b) {
        return new BufferPacketData(b);
    }

    static BufferPacketData wrap(byte[] buf, int off, int len) {
        return new BufferPacketData(ByteBuffer.wrap(buf, off, len));
    }

    //

    int size();

    ByteOrder byteOrder();

    void byteOrder(ByteOrder byteOrder);

    default byte[] toBytes() {
        byte[] ret = new byte[this.size()];
        this.toBytes(ret, 0);
        return ret;
    }

    void toBytes(byte[] buf, int off);

    default void write(DataOutputStream dos) throws IOException {
        dos.write(this.toBytes());
    }

    //

    String ERR_STUB = "Packet data is not ";
    String ERR_READABLE = "readable";
    String ERR_WRITABLE = "writable";

    //

    default byte readInt8() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default int readUInt8() throws UnsupportedOperationException {
        return Byte.toUnsignedInt(this.readInt8());
    }

    default short readInt16() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default int readUInt16() throws UnsupportedOperationException {
        return Short.toUnsignedInt(this.readInt16());
    }

    default int readInt32() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default long readUInt32() throws UnsupportedOperationException {
        return Integer.toUnsignedLong(this.readInt32());
    }

    default long readInt64() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default float readFloat32() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default double readFloat64() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default boolean readBoolean() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default char readChar() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default String readUTF8() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default byte[] readLiteral(int len) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default byte[] readRemaining() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    default byte[] readRemainingUpTo(int len) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_READABLE);
    }

    //

    default void writeInt8(int int8) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeUInt8(int uint8) throws UnsupportedOperationException {
        this.writeInt8(uint8 & 0xFF);
    }

    default void writeInt16(int int16) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeUInt16(int uint16) throws UnsupportedOperationException {
        this.writeInt16(uint16 & 0xFFFF);
    }

    default void writeInt32(int int32) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeUInt32(long uint32) throws UnsupportedOperationException {
        this.writeInt32((int) uint32);
    }

    default void writeInt64(long int64) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeFloat32(float float32) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeFloat64(double float64) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeBoolean(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeChar(char c) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeUTF8(String utf) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

    default void writeLiteral(byte[] literal) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ERR_STUB + ERR_WRITABLE);
    }

}
