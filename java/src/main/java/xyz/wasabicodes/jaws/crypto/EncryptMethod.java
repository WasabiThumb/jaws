package xyz.wasabicodes.jaws.crypto;

import xyz.wasabicodes.jaws.crypto.nacl.SodiumEncryptHandler;
import xyz.wasabicodes.jaws.crypto.none.NoneEncryptHandler;
import xyz.wasabicodes.jaws.crypto.optimus.OptimusEncryptHandler;
import xyz.wasabicodes.jaws.crypto.roxor.RoxorEncryptHandler;

public enum EncryptMethod implements EncryptHandler {
    NONE(0, new NoneEncryptHandler()),
    ROXOR(1, new RoxorEncryptHandler()),
    NACL(2, new SodiumEncryptHandler()),
    OPTIMUS(3, new OptimusEncryptHandler());

    public static EncryptMethod get(byte id) {
        return switch (id) {
            case 1 -> ROXOR;
            case 2 -> NACL;
            default -> NONE;
        };
    }

    private final byte id;
    private final EncryptHandler handler;
    EncryptMethod(int id, EncryptHandler handler) {
        this.id = (byte) id;
        this.handler = handler;
    }

    public byte getIdentifier() {
        return this.id;
    }

    @Override
    public int getKeySize() {
        return this.handler.getKeySize();
    }

    @Override
    public int getMaxHeaderSize() {
        return this.handler.getMaxHeaderSize();
    }

    @Override
    public EncryptKey generateLocalKey() {
        return this.handler.generateLocalKey();
    }

    @Override
    public EncryptKey importRemoteKey(byte[] data) throws IllegalArgumentException {
        return this.handler.importRemoteKey(data);
    }

    @Override
    public byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        return this.handler.encrypt(data, senderKey, receiverKey);
    }

    @Override
    public byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        return this.handler.decrypt(data, senderKey, receiverKey);
    }
}
