import Packet from "../../packet";
import {PacketData} from "../../data";
import {ByteOrder, NATIVE_ORDER} from "../../../util/data";
import {EncryptKey} from "../../../crypto/struct";
import * as utf8 from "@stablelib/utf8";
import {FriendlyName} from "../../../util/string";


export default class PacketInLogin extends Packet {

    order: ByteOrder = NATIVE_ORDER;
    name: string = "USERNAME";
    keyData: EncryptKey | Uint8Array = new Uint8Array(0);
    constructor() {
        super(1);
    }

    getElementCount(): number {
        return 3;
    }

    read(dat: PacketData): void {
        const flag: number = dat.readUInt8();
        const len: number = flag & 127;
        if (len < 1) throw new Error("Login packet username is empty");
        const bytes: Uint8Array = dat.readLiteral(len);
        if (!FriendlyName.isLegal(bytes)) throw new Error("Login packet username is invalid");
        this.order = ((flag & 128) === 0) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        this.name = utf8.decode(bytes);
        this.keyData = dat.readRemaining();
    }

    write(dat: PacketData): void {
        const bytes: Uint8Array = utf8.encode(this.name);
        let len: number = bytes.length;
        if (len > 96 || len < 1) throw Error("Login packet username is invalid length (" + len + ")");
        if (this.order === ByteOrder.BIG_ENDIAN) len |= 128;
        const kd = this.keyData;
        dat.writeUInt8(len);
        dat.writeLiteral(bytes);
        dat.writeLiteral(kd instanceof Uint8Array ? kd : kd.export());
    }

}
