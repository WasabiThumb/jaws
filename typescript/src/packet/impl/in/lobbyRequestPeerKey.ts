import Packet from "../../packet";
import UUID from "../../../util/uuid";
import Long from "long";
import {PacketData} from "../../data";

export default class PacketInLobbyRequestPeerKey extends Packet {

    transaction: number = 0;
    peerID: UUID = new UUID(Long.ZERO, Long.ZERO);
    constructor() {
        super(8);
    }

    getElementCount(): number {
        return 3;
    }

    read(dat: PacketData): void {
        this.transaction = dat.readUInt8();
        const mostSig: Long = dat.readInt64();
        const leastSig: Long = dat.readInt64();
        this.peerID = new UUID(mostSig, leastSig);
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.transaction);
        dat.writeInt64(this.peerID.mostSignificantBits);
        dat.writeInt64(this.peerID.leastSignificantBits);
    }

}
