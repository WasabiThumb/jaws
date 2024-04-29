package xyz.wasabicodes.jaws.crypto.none;

import xyz.wasabicodes.jaws.crypto.EncryptHandler;
import xyz.wasabicodes.jaws.crypto.EncryptKey;

public class NoneEncryptHandler implements EncryptHandler {

    @Override
    public int getKeySize() {
        return 0;
    }

    @Override
    public EncryptKey generateLocalKey() {
        return NoneEncryptKey.INSTANCE;
    }

    @Override
    public EncryptKey importRemoteKey(byte[] data) {
        return NoneEncryptKey.INSTANCE;
    }

    @Override
    public byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) {
        return data;
    }

}
