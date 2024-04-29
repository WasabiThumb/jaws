package xyz.wasabicodes.jaws.compress;

import xyz.wasabicodes.jaws.compress.none.NoneCompressHandler;
import xyz.wasabicodes.jaws.compress.zlib.ZLibCompressHandler;

public enum CompressMethod implements CompressHandler {
    NONE(0, new NoneCompressHandler()),
    ZLIB(1, new ZLibCompressHandler());

    public static CompressMethod get(byte id) {
        return (id == (byte) 1) ? ZLIB : NONE;
    }

    private final byte id;
    private final CompressHandler handler;
    CompressMethod(int id, CompressHandler handler) {
        this.id = (byte) id;
        this.handler = handler;
    }

    public byte getIdentifier() {
        return this.id;
    }

    @Override
    public byte[] compress(byte[] bytes) {
        return this.handler.compress(bytes);
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IllegalArgumentException {
        return this.handler.decompress(bytes);
    }

}
