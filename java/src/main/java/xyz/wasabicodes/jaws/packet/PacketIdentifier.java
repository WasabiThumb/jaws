package xyz.wasabicodes.jaws.packet;

public interface PacketIdentifier {

    static PacketIdentifier from(byte b) {
        return new Basic(b);
    }

    //

    byte toByte();

    default byte[] toBytes() {
        return new byte[] { this.toByte() };
    }

    //

    record Basic(byte b) implements PacketIdentifier {

        @Override
        public byte toByte() {
            return this.b;
        }

    }

}
