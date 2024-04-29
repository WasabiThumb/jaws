package xyz.wasabicodes.jaws.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public interface EncryptHandler {

    int getKeySize();

    default int getMaxHeaderSize() {
        return 0;
    }

    EncryptKey generateLocalKey();

    EncryptKey importRemoteKey(byte[] data) throws IllegalStateException;

    byte[] encrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException;

    default String encryptString(String data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        byte[] encrypted = this.encrypt(data.getBytes(StandardCharsets.UTF_8), senderKey, receiverKey);
        encrypted = Base64.getEncoder().encode(encrypted);
        return new String(encrypted, StandardCharsets.UTF_8);
    }

    byte[] decrypt(byte[] data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException;

    default String decryptString(String data, EncryptKey senderKey, EncryptKey receiverKey) throws IllegalArgumentException {
        byte[] decrypted = Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
        decrypted = this.decrypt(decrypted, senderKey, receiverKey);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

}
