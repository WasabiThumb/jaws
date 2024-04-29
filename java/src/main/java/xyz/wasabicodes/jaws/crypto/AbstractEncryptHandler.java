package xyz.wasabicodes.jaws.crypto;

import xyz.wasabicodes.jaws.util.ByteUnaryOperator;

public abstract class AbstractEncryptHandler implements EncryptHandler {

    protected <T extends EncryptKey> T assertKeyType(Class<T> clazz, EncryptKey key) throws IllegalArgumentException {
        if (!clazz.isInstance(key)) throw new IllegalArgumentException("Key " + key + " is not of type " + clazz.getSimpleName() + " as required by " + this.getClass().getSimpleName());
        return clazz.cast(key);
    }

    protected byte[] unaryBytes(byte[] data, ByteUnaryOperator fn) {
        byte[] ret = new byte[data.length];
        for (int i=0; i < data.length; i++) ret[i] = fn.applyByte(data[i]);
        return ret;
    }

}
