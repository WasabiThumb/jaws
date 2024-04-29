package xyz.wasabicodes.jaws.compress.zlib;

import xyz.wasabicodes.jaws.compress.CompressHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public class ZLibCompressHandler implements CompressHandler {

    @Override
    public byte[] compress(byte[] bytes) {
        byte[] res;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
                try (DeflaterInputStream dis = new DeflaterInputStream(bis)) {
                    byte[] shovel = new byte[8192];
                    int read;
                    while ((read = dis.read(shovel)) != -1) {
                        bos.write(shovel, 0, read);
                    }
                }
            }
            res = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return res;
    }

    @Override
    public byte[] decompress(byte[] bytes) throws IllegalArgumentException {
        byte[] ret;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (InflaterInputStream iis = new InflaterInputStream(bis)) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length)) {
                    byte[] shovel = new byte[8192];
                    int read;
                    while ((read = iis.read(shovel)) != -1) {
                        bos.write(shovel, 0, read);
                    }
                    ret = bos.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return ret;
    }

}
