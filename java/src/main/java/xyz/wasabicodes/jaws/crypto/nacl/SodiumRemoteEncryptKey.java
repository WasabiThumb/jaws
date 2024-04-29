package xyz.wasabicodes.jaws.crypto.nacl;

import java.util.Arrays;

record SodiumRemoteEncryptKey(byte[] publicKey) implements SodiumEncryptKey {

    @Override
    public byte[] export() {
        return Arrays.copyOf(this.publicKey, this.publicKey.length);
    }

    @Override
    public byte[] getPublic() {
        return this.publicKey;
    }

}
