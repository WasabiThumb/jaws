package xyz.wasabicodes.jaws.struct.lobby;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class LobbyCode {

    public static final char[] LEGAL_CHARS = new char[] {
            '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', '0', '1'
    };

    private static final int[] C2I;
    static {
        int len = ('y' - '0') + 1;
        int[] arr = new int[len];
        Arrays.fill(arr, -1);

        char c;
        for (int i=0; i < LEGAL_CHARS.length; i++) {
            c = LEGAL_CHARS[i];
            arr[c - '0'] = i;
            if (c < '2') {
                if (c == '0') {
                    arr['O' - '0'] = i;
                    arr['o' - '0'] = i;
                } else {
                    arr['I' - '0'] = i;
                    arr['L' - '0'] = i;
                    arr['i' - '0'] = i;
                    arr['l' - '0'] = i;
                }
            } else if (c > '9') {
                arr[c - 16] = i;
            }
        }
        C2I = arr;
    }

    //

    public static String generate() {
        Random r = ThreadLocalRandom.current();
        char[] chars = new char[6];
        for (int i=0; i < 6; i++) chars[i] = LEGAL_CHARS[r.nextInt(LEGAL_CHARS.length)];
        return new String(chars);
    }

    public static int toInt(String s) {
        char[] c = s.toCharArray();
        if (c.length != 6) return -1;
        int ret = 0;
        int idx;
        for (char value : c) {
            idx = getCharIndex(value);
            if (idx == -1) return -1;
            ret <<= 5;
            ret |= idx;
        }
        return ret;
    }

    public static String fromInt(int i) {
        char[] ret = new char[6];
        for (int z=0; z < 6; z++) {
            ret[5 - z] = LEGAL_CHARS[i & 31];
            i >>= 5;
        }
        return new String(ret);
    }

    private static int getCharIndex(char c) {
        if (c < '0' || c > 'y') return -1;
        return C2I[c - '0'];
    }

}
