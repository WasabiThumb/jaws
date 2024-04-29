package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.util.FriendlyName;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketInLogin extends PacketIn {

    public ByteOrder order = ByteOrder.BIG_ENDIAN;
    public String name = "USERNAME";
    public byte[] keyData = new byte[0];
    public PacketInLogin() {
        super(1);
    }

    public void setKey(EncryptKey key) {
        this.keyData = key.export();
    }

    @Override
    public int getElementCount() {
        return 3;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        final int flag = dat.readUInt8();
        final int len = flag & 127;
        if (len < 1) throw new IllegalArgumentException("Login packet username is empty");
        final byte[] bytes = dat.readLiteral(len);
        if (!FriendlyName.isLegal(bytes)) throw new IllegalArgumentException("Login packet username is invalid (" + new String(bytes, StandardCharsets.UTF_8) + ")");
        this.order = ((flag & 128) == 0) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        this.name = new String(bytes, StandardCharsets.UTF_8);
        this.keyData = dat.readRemaining();
    }

    @Override
    public void write(PacketData dat) {
        final byte[] bytes = this.name.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        if (len > 96 || len < 1) throw new IllegalStateException("Login packet username is invalid length (" + len + " bytes)");
        if (this.order == ByteOrder.BIG_ENDIAN) len |= 128;
        dat.writeUInt8(len);
        dat.writeLiteral(bytes);
        dat.writeLiteral(this.keyData);
    }

    @Override
    public boolean transmitPlain() {
        return true;
    }

}
