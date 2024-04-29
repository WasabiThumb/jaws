import Packet from "../../packet";
import {PacketData} from "../../data";

export default class PacketInPing extends Packet {

    readonly data: Uint8Array = new Uint8Array(512);
    constructor() {
        super(3);
    }

    getElementCount(): number {
        return 1;
    }

    read(dat: PacketData): void {
        const read: Uint8Array = dat.readLiteral(512);
        this.data.set(read, 0);
    }

    write(dat: PacketData): void {
        dat.writeLiteral(this.data);
    }

}
