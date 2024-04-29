package xyz.wasabicodes.jaws.compress;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.wasabicodes.jaws.packet.data.PacketData;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CompressMethodTest {

    private static Random RAND;

    @BeforeAll
    static void setup() {
        RAND = new Random();
    }

    @Test
    void all() {
        System.out.println("Testing all compression methods");
        for (CompressMethod cm : CompressMethod.values()) {
            System.out.println(' ');
            this.single(cm);
            System.out.println(' ');
        }
    }

    void single(CompressMethod method) {
        System.out.println("Method: " + method.name());

        PacketData pd = PacketData.create(32);
        for (int i=0; i < 32; i++) pd.writeInt32(RAND.nextInt(0xFFFF));
        byte[] bytes = pd.toBytes();
        System.out.println("Uncompressed Data: " + hexBytes(bytes));

        byte[] compressed = method.compress(bytes);
        System.out.println("Compressed Data: " + hexBytes(compressed));

        byte[] decompressed = method.decompress(compressed);
        System.out.println("Decompressed Data: " + hexBytes(decompressed));

        assertArrayEquals(bytes, decompressed);
        System.out.println("Data matches!");
    }

    static final char[] HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    String hexBytes(byte[] bytes) {
        char[] out = new char[(bytes.length << 1) + (bytes.length - 1)];
        int b;
        int z = 0;
        for (byte ab : bytes) {
            if (z != 0) out[z++] = ' ';
            b = ab & 0xFF;
            out[z++] = HEX_CHARS[b >> 4];
            out[z++] = HEX_CHARS[b & 0xF];
        }
        return new String(out);
    }

}