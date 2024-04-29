package xyz.wasabicodes.jaws.crypto.none;

import xyz.wasabicodes.jaws.crypto.EncryptKey;

class NoneEncryptKey implements EncryptKey {

    public static final NoneEncryptKey INSTANCE = new NoneEncryptKey();

    @Override
    public byte[] export() {
        return new byte[0];
    }

}
