package xyz.wasabicodes.jaws.crypto.roxor;

import xyz.wasabicodes.jaws.crypto.EncryptKey;

class RoxorEncryptKey implements EncryptKey {

    final byte[] data;
    RoxorEncryptKey(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] export() {
        return this.data;
    }

    public byte encrypt(byte b) {
        int shift;
        int mask;
        for (byte token : this.data) {
            shift = (token & 0xFF) >> 5;
            mask = (token & 0x1F);
            b = (byte) (ror(b, shift) ^ mask);
        }
        return b;
    }

    public byte decrypt(byte b) {
        int shift;
        int mask;
        byte token;
        for (int i=this.data.length - 1; i >= 0; i--) {
            token = this.data[i];
            shift = (token & 0xE0) >> 5;
            mask = (token & 0x1F);
            b = rol((byte) (b ^ mask), shift);
        }
        return b;
    }

    private byte ror(byte bits, int shift) {
        if (shift == 0) return bits;
        int b = bits & 0xFF;
        return (byte) ((b >> shift) | ((b & ((1 << shift) - 1)) << (8 - shift)));
    }

    private byte rol(byte bits, int shift) {
        if (shift == 0) return bits;
        int b = bits & 0xFF;
        return (byte) (((b << shift) & 0xFF) | (b >> (8 - shift)));
    }

}
