package xyz.wasabicodes.jaws.crypto.nacl;

import com.goterl.lazysodium.SodiumJava;
import xyz.wasabicodes.jaws.crypto.AbstractEncryptHandler;
import xyz.wasabicodes.jaws.crypto.EncryptKey;

public class SodiumEncryptHandler extends AbstractEncryptHandler {

    private final SodiumJava sodium = new SodiumJava();

    @Override
    public int getKeySize() {
        return 32;
    }

    @Override
    public int getMaxHeaderSize() {
        return 40;
    }

    @Override
    public EncryptKey generateLocalKey() {
        byte[] pk = new byte[32];
        byte[] sk = new byte[32];
        this.sodium.crypto_box_keypair(pk, sk);
        return new SodiumLocalEncryptKey(pk, sk);
    }

    @Override
    public EncryptKey importRemoteKey(byte[] data) throws IllegalArgumentException {
        if (data.length != 32) throw new IllegalArgumentException("Public key is not 32 bytes (got " + data.length + ")");
        return new SodiumRemoteEncryptKey(data);
    }

    @Override
    public byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        byte[] sender = this.assertKeyType(SodiumLocalEncryptKey.class, senderKey).getPrivate();
        byte[] receiver = this.assertKeyType(SodiumEncryptKey.class, receiverKey).getPublic();

        byte[] out = new byte[data.length + 40];
        byte[] nonce = new byte[24];
        this.sodium.randombytes_buf(nonce, 24);

        int res = this.sodium.crypto_box_easy(out, data, data.length, nonce, receiver, sender);
        if (res != 0) throw new IllegalStateException("Error " + res + " in crypto_box_easy");

        System.arraycopy(out, 0, out, 24, data.length + 16);
        System.arraycopy(nonce, 0, out, 0, 24);
        return out;
    }

    @Override
    public byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        if (data.length < 40) throw new IllegalArgumentException("Data is less than 40 bytes long");

        int rem = data.length - 24;
        byte[] body = new byte[rem];
        System.arraycopy(data, 24, body, 0, rem);

        byte[] sender = this.assertKeyType(SodiumEncryptKey.class, senderKey).getPublic();
        byte[] receiver = this.assertKeyType(SodiumLocalEncryptKey.class, receiverKey).getPrivate();

        byte[] ret = new byte[rem - 16];
        int res = this.sodium.crypto_box_open_easy(ret, body, body.length, data, sender, receiver);
        if (res != 0) throw new IllegalArgumentException("Error " + res + " in crypto_box_open_easy");

        return ret;
    }

}
