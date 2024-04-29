package xyz.wasabicodes.jaws.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AOInt2ObjectMapTest {

    @Test
    void test() {
        AOInt2ObjectMap<Integer> map = new AOInt2ObjectMap<>(Integer.class, 4);

        assertEquals(map.size(), 0);

        for (int i=3; i < 11; i++) {
            map.put(i, Integer.valueOf(i));
        }

        assertEquals(map.size(), 8);

        for (int i=3; i < 11; i++) {
            assertEquals(i, map.get(i).intValue());
        }

        for (int i=3; i < 9; i++) {
            assertEquals(map.remove(i), Integer.valueOf(i));
        }

        for (int i=3; i < 9; i++) {
            assertEquals(map.get(i), map.defaultReturnValue());
        }

        for (int i=9; i < 11; i++) {
            assertEquals(i, map.get(i).intValue());
        }

        assertEquals(map.size(), 2);

        map.clear();

        assertEquals(map.size(), 0);
    }

}