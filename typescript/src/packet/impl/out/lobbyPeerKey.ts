import Packet from "../../packet";
import {PacketData} from "../../data";
import UUID from "../../../util/uuid";
import Long from "long";

export default class PacketOutLobbyPeerKey extends Packet {

    transaction: number = 0;
    peerID: UUID = new UUID(Long.ZERO, Long.ZERO);
    keyData: Uint8Array = new Uint8Array(0);
    constructor() {
        super(9);
    }

    getElementCount(): number {
        return 4;
    }

    read(dat: PacketData): void {
        this.transaction = dat.readUInt8();
        const mostSig: Long = dat.readInt64();
        const leastSig: Long = dat.readInt64();
        this.peerID = new UUID(mostSig, leastSig);
        this.keyData = dat.readRemaining();
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.transaction);
        dat.writeInt64(this.peerID.mostSignificantBits);
        dat.writeInt64(this.peerID.leastSignificantBits);
        dat.writeLiteral(this.keyData);
    }

}
