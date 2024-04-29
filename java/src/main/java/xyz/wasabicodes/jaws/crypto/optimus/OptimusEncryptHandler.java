package xyz.wasabicodes.jaws.crypto.optimus;

import xyz.wasabicodes.jaws.crypto.AbstractEncryptHandler;
import xyz.wasabicodes.jaws.crypto.EncryptKey;
import xyz.wasabicodes.jaws.util.Optimus;

public class OptimusEncryptHandler extends AbstractEncryptHandler {

    @Override
    public int getKeySize() {
        return 3;
    }

    @Override
    public EncryptKey generateLocalKey() {
        return new OptimusEncryptKey(Optimus.generate(8));
    }

    @Override
    public EncryptKey importRemoteKey(byte[] data) throws IllegalStateException {
        Optimus opt = Optimus.deserialize(data);
        return new OptimusEncryptKey(opt);
    }

    @Override
    public byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        final OptimusEncryptKey key = this.assertKey(senderKey);
        return this.unaryBytes(data, key::encode);
    }

    @Override
    public byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        final OptimusEncryptKey key = this.assertKey(senderKey);
        return this.unaryBytes(data, key::decode);
    }

    private OptimusEncryptKey assertKey(EncryptKey key) throws IllegalArgumentException {
        return this.assertKeyType(OptimusEncryptKey.class, key);
    }

}
