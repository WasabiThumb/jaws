package xyz.wasabicodes.jaws.compress;

public interface CompressHandler {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes) throws IllegalArgumentException;

}
