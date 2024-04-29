import Packet from "../../packet";
import {PacketData} from "../../data";
import {FriendlyName} from "../../../util/string";
import * as utf8 from "@stablelib/utf8";


export default class PacketInLobbyCreate extends Packet {

    transaction: number = 0;
    name: string = "New Lobby";
    constructor() {
        super(5);
    }

    getElementCount(): number {
        return 3;
    }

    read(dat: PacketData): void {
        const transaction: number = dat.readUInt8();
        const len: number = (dat.readUInt8() & 0x1F) + 1;
        const bytes: Uint8Array = dat.readRemainingUpTo((len << 2) - len);
        if (!FriendlyName.isLegal(bytes)) throw new Error("Illegal lobby name");

        this.transaction = transaction;
        this.name = utf8.decode(bytes);
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.transaction);
        dat.writeUInt8((this.name.length - 1) & 0x1F);
        dat.writeLiteral(utf8.encode(this.name));
    }

}
