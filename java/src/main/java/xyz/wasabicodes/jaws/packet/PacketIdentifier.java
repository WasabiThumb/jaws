package xyz.wasabicodes.jaws.packet;

public interface PacketIdentifier extends Comparable<PacketIdentifier> {

    static PacketIdentifier from(byte b) {
        return new Basic(b);
    }

    //

    byte toByte();

    default byte[] toBytes() {
        return new byte[] { this.toByte() };
    }

    /**
     * Returns a negative integer, zero, or a positive integer as this identifier is less than, equal to, or greater
     * than the specified integer.
     */
    default int compareTo(int b) {
        return this.compareTo(new PacketIdentifier.Basic((byte) b));
    }

    default boolean inRange(int a, int b) {
        if (this.compareTo(a) < 0) return false;
        return this.compareTo(b) <= 0;
    }

    //

    record Basic(byte b) implements PacketIdentifier {

        @Override
        public byte toByte() {
            return this.b;
        }

        @Override
        public int compareTo(PacketIdentifier other) {
            if (other instanceof Basic ob) {
                return Byte.compare(this.b, ob.b);
            }
            throw new IllegalArgumentException("Cannot compare PacketIdentifier.Basic(" +
                    Byte.toUnsignedInt(b) + ") and " + other);
        }

        @Override
        public int compareTo(int b) {
            return Integer.compare(Byte.toUnsignedInt(this.b), b);
        }

    }

}
