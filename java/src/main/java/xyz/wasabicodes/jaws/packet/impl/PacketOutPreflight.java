package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.compress.CompressMethod;
import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.crypto.EncryptMethod;
import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;

import java.nio.ByteOrder;

public class PacketOutPreflight extends PacketOut {

    public byte protocolVersion = (byte) 0;
    public EncryptMethod encryption = EncryptMethod.NONE;
    public CompressMethod compression = CompressMethod.NONE;
    public ByteOrder preferredOrder = ByteOrder.nativeOrder();
    public EncryptKey serverKey;
    public PacketOutPreflight() {
        super(0);
    }

    @Override
    public int getElementCount() {
        return 5;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        byte protocolVersion = dat.readInt8();
        if ((protocolVersion & 0xFF) == 255) throw new IllegalArgumentException("Preflight packet data has reserved protocol version (" + protocolVersion + ")");
        byte encryption = dat.readInt8();
        byte compression = dat.readInt8();
        byte order = dat.readInt8();

        EncryptMethod encryptMethod = EncryptMethod.get(encryption);
        byte[] keyData = dat.readLiteral(encryptMethod.getKeySize());

        this.protocolVersion = protocolVersion;
        this.encryption = encryptMethod;
        this.compression = CompressMethod.get(compression);
        this.preferredOrder = (order == (byte) 1) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        this.serverKey = encryptMethod.importRemoteKey(keyData);
    }

    @Override
    public void write(PacketData dat) {
        dat.writeInt8(this.protocolVersion);
        dat.writeInt8(this.encryption.getIdentifier());
        dat.writeInt8(this.compression.getIdentifier());
        dat.writeInt8(this.preferredOrder == ByteOrder.BIG_ENDIAN ? 1 : 0);
        dat.writeLiteral(this.serverKey.export());
    }

    @Override
    public boolean transmitPlain() {
        return true;
    }

}
