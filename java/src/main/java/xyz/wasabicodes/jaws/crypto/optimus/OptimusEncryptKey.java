package xyz.wasabicodes.jaws.crypto.optimus;

import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.util.Optimus;

public class OptimusEncryptKey implements EncryptKey {

    private final Optimus optimus;
    OptimusEncryptKey(Optimus optimus) {
        this.optimus = optimus;
    }

    public byte encode(byte in) {
        return (byte) this.optimus.encode(in);
    }

    public byte decode(byte in) {
        return (byte) this.optimus.decode(in);
    }

    @Override
    public byte[] export() {
        byte[] b = this.optimus.serialize();
        if (b.length != 3) throw new IllegalStateException("Optimus data is greater than 3 bytes");
        return b;
    }

}
