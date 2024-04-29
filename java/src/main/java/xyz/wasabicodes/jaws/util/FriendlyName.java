package xyz.wasabicodes.jaws.util;

import java.nio.charset.StandardCharsets;

public final class FriendlyName {

    // Count the number of codepoints in a string, or 0 if any codepoint does not fall in the range U+0020 to U+FFFF,
    // or the sequence is deemed invalid.
    public static int count(byte[] utf) {
        int skip = 0;
        int len = 0;
        int n;
        for (byte b : utf) {
            if (skip > 0) {
                skip--;
                continue;
            }
            n = (b & 240) >> 4;
            switch (n) {
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    break;
                case 12:
                case 13:
                    skip = 1;
                    break;
                case 14:
                    skip = 2;
                    break;
                default:
                    return 0;
            }
            len++;
        }
        if (skip != 0) return 0;
        return len;
    }

    public static boolean isLegal(String name) {
        return isLegal(name.getBytes(StandardCharsets.UTF_8));
    }

    // Check if 1 to 32 codepoints are present, each falling in the range U+0020 to U+FFFF.
    public static boolean isLegal(byte[] utf) {
        final int c = count(utf);
        return c > 0 && c <= 32;
    }

}