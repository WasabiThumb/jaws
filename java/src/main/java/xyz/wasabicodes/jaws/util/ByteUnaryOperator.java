package xyz.wasabicodes.jaws.util;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface ByteUnaryOperator extends UnaryOperator<Byte> {

    byte applyByte(byte aByte);

    @Override
    default Byte apply(Byte aByte) {
        return this.applyByte(aByte);
    }

}
