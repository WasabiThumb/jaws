package xyz.wasabicodes.jaws.packet;

public abstract class PacketOut extends Packet {

    public PacketOut(PacketIdentifier id) {
        super(id);
    }

    public PacketOut(int id) {
        super(id);
    }

    @Override
    public final PacketDirection getDirection() {
        return PacketDirection.OUT;
    }

}
