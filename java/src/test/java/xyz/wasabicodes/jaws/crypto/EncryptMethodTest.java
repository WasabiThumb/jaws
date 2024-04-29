package xyz.wasabicodes.jaws.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

class EncryptMethodTest {

    @Test
    public void all() {
        System.out.println("Testing all encryption methods");
        for (EncryptMethod method : EncryptMethod.values()) {
            System.out.println(' ');
            single(method);
            System.out.println(' ');
        }
    }

    private void single(EncryptMethod method) {
        System.out.println("Testing method: " + method.name());
        EncryptKey senderKey = method.generateLocalKey();
        System.out.println("Sender key: " + Arrays.toString(senderKey.export()));
        EncryptKey receiverKey = method.generateLocalKey();
        System.out.println("Receiver key: " + Arrays.toString(receiverKey.export()));

        String message = generateMessage();
        System.out.println("Original message: " + message);

        String encrypted = method.encryptString(message, senderKey, receiverKey);
        System.out.println("Encrypted message: " + encrypted);

        String decrypted = method.decryptString(encrypted, senderKey, receiverKey);
        System.out.println("Decrypted message: " + decrypted);

        assertArrayEquals(message.getBytes(StandardCharsets.UTF_8), decrypted.getBytes(StandardCharsets.UTF_8));
    }

    private String generateMessage() {
        return "Super Secret Message (" + ThreadLocalRandom.current().nextInt() + ")";
    }

}