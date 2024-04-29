import Packet from "../../packet";
import {PacketData} from "../../data";
import UUID from "../../../util/uuid";
import Long from "long";

export default class PacketOutSessionStart extends Packet {

    identifier: UUID = new UUID(Long.ZERO, Long.ZERO);
    constructor() {
        super(2);
    }

    getElementCount(): number {
        return 2;
    }

    read(dat: PacketData): void {
        const mostSig: Long = dat.readInt64();
        const leastSig: Long = dat.readInt64();
        this.identifier = new UUID(mostSig, leastSig);
    }

    write(dat: PacketData): void {
        dat.writeInt64(this.identifier.mostSignificantBits);
        dat.writeInt64(this.identifier.leastSignificantBits);
    }

}
