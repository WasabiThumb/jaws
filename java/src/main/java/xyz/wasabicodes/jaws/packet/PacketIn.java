package xyz.wasabicodes.jaws.packet;

public abstract class PacketIn extends Packet {

    public PacketIn(PacketIdentifier id) {
        super(id);
    }

    public PacketIn(int id) {
        super(id);
    }

    @Override
    public final PacketDirection getDirection() {
        return PacketDirection.IN;
    }

}
