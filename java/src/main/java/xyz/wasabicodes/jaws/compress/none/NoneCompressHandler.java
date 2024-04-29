package xyz.wasabicodes.jaws.compress.none;

import xyz.wasabicodes.jaws.compress.CompressHandler;

public class NoneCompressHandler implements CompressHandler {

    @Override
    public byte[] compress(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        return bytes;
    }

}
