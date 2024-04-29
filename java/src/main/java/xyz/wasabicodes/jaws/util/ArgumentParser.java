package xyz.wasabicodes.jaws.util;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;

import java.util.Locale;


public class ArgumentParser {

    private final String[] array;
    private final int length;
    private final Object2ByteMap<String> parsed;
    private int head = 0;
    private boolean definitelyComplete = false;
    public ArgumentParser(String[] args) {
        if (args.length > 255) throw new RuntimeException("Command line arguments too long to parse (greater than 255)");
        this.array = args;
        this.length = args.length;
        this.parsed = new Object2ByteOpenHashMap<>(args.length);
        this.parsed.defaultReturnValue((byte) 0);
    }

    public String get(String key) {
        int idx = this.get0(key.toLowerCase(Locale.ROOT));
        if (idx == 0) return null;
        return this.array[idx];
    }

    public boolean getFlag(String key) {
        key = key.toLowerCase(Locale.ROOT);
        int idx = this.get0(key);
        if (idx == 0) return this.parsed.containsKey(key);
        String v = this.array[idx];
        return switch (v.length()) {
            case 0 -> true;
            case 1 -> v.charAt(0) != '0';
            default -> !v.equalsIgnoreCase("false");
        };
    }

    private int get0(String key) {
        int ret = Byte.toUnsignedInt(this.parsed.getByte(key));
        if (this.definitelyComplete || ret != 0) return ret;

        String token;
        String value;
        int valueIdx;
        while (this.head < this.length) {
            token = this.array[this.head++].toLowerCase(Locale.ROOT);
            token = this.removeDoubleDash(token);
            if (this.head < this.length) {
                value = this.array[valueIdx = this.head];
                if (value.isEmpty() || value.charAt(0) == '-') {
                    valueIdx = 0;
                } else {
                    this.head++;
                }
            } else {
                valueIdx = 0;
            }
            this.parsed.put(token, (byte) valueIdx);
            if (token.equals(key)) return valueIdx;
        }

        this.definitelyComplete = true;
        return 0;
    }

    private static final String ERR_BAD_SWITCH = "Invalid command line switch: ";
    private String removeDoubleDash(String key) {
        char[] chars = key.toCharArray();
        if (chars.length < 2) throw new RuntimeException(ERR_BAD_SWITCH + key);

        int off = 0;
        while (off < 2) {
            if (chars[off] != '-') {
                if (off == 0) throw new RuntimeException(ERR_BAD_SWITCH + key);
                break;
            }
            off++;
        }

        return new String(chars, off, chars.length - off);
    }

}
