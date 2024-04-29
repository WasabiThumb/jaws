package xyz.wasabicodes.jaws.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OptimusTest {

    @Test
    void test() {
        Random r = new Random();
        long start = System.nanoTime();
        for (int i = 3; i < 32; i++) {
            final Optimus opt = Optimus.generate(r, i);

            assertTrue(opt.encode(opt.max() >> 1) <= opt.max());
            final int sample = r.nextInt(opt.max());
            assertEquals(opt.decode(opt.encode(sample)), sample);

            for (int ord=0; ord < 2; ord++) {
                final byte[] serialized = opt.serialize(ord == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                System.out.println(i + " : " + Arrays.toString(serialized));
                final Optimus de = assertDoesNotThrow(() -> Optimus.deserialize(serialized));
                assertEquals(de, opt);
            }
        }
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
    }

}