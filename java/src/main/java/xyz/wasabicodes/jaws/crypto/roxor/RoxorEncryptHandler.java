package xyz.wasabicodes.jaws.crypto.roxor;

import xyz.wasabicodes.jaws.crypto.AbstractEncryptHandler;
import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.util.ByteUnaryOperator;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class RoxorEncryptHandler extends AbstractEncryptHandler {

    @Override
    public int getKeySize() {
        return 16;
    }

    @Override
    public EncryptKey generateLocalKey() {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES * 4);
        SecureRandom sr = new SecureRandom();
        for (int i=0; i < 4; i++) bb.putInt(sr.nextInt());
        return new RoxorEncryptKey(bb.array());
    }

    @Override
    public EncryptKey importRemoteKey(byte[] data) throws IllegalArgumentException {
        if (data.length != 16) throw new IllegalArgumentException("Data is not 16 bytes long");
        return new RoxorEncryptKey(data);
    }

    @Override
    public byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        RoxorEncryptKey key = this.assertKey(senderKey);
        return this.unaryBytes(data, key::encrypt);
    }

    @Override
    public byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        RoxorEncryptKey key = this.assertKey(senderKey);
        return this.unaryBytes(data, key::decrypt);
    }

    private RoxorEncryptKey assertKey(EncryptKey key) throws IllegalArgumentException {
        return this.assertKeyType(RoxorEncryptKey.class, key);
    }

}
