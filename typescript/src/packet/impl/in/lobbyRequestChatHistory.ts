import Packet from "../../packet";
import {PacketData} from "../../data";

export default class PacketInLobbyRequestChatHistory extends Packet {

    transaction: number = 0;
    constructor() {
        super(12);
    }

    getElementCount(): number {
        return 1;
    }

    read(dat: PacketData): void {
        this.transaction = dat.readInt32();
    }

    write(dat: PacketData): void {
        dat.writeInt32(this.transaction);
    }

}
