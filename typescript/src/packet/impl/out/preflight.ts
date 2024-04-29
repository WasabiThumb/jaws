import Packet from "../../packet";
import {PacketData} from "../../data";
import {EncryptHandler, EncryptKey} from "../../../crypto/struct";
import EncryptMethod from "../../../crypto/method";
import {CompressHandler} from "../../../compress/struct";
import CompressMethod from "../../../compress/method";
import {ByteOrder} from "../../../util/data";

export default class PacketOutPreflight extends Packet {

    protocolVersion: number = 0;
    encryption: EncryptHandler = EncryptMethod.NONE;
    compression: CompressHandler = CompressMethod.NONE;
    preferredOrder: ByteOrder = ByteOrder.BIG_ENDIAN;
    serverKey: EncryptKey | null = null;
    constructor() {
        super(0);
    }

    getElementCount(): number {
        return 5;
    }

    read(dat: PacketData): void {
        const protocolVersion: number = dat.readUInt8();
        if (protocolVersion === 255) throw new Error("Preflight packet data has reserved protocol version (" + protocolVersion + ")");
        const encryption: number = dat.readInt8();
        const compression: number = dat.readInt8();
        const order: number = dat.readInt8();

        const encryptMethod: EncryptHandler = EncryptMethod.get(encryption);
        const keyData: Uint8Array = dat.readLiteral(encryptMethod.keySize);

        this.protocolVersion = protocolVersion;
        this.encryption = encryptMethod;
        this.compression = CompressMethod.get(compression);
        this.preferredOrder = order === 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        this.serverKey = encryptMethod.importRemoteKey(keyData);
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.protocolVersion);
        dat.writeInt8(this.encryption.identifier);
        dat.writeInt8(this.compression.identifier);
        dat.writeInt8(this.preferredOrder === ByteOrder.BIG_ENDIAN ? 1 : 0);
        dat.writeLiteral(this.serverKey!.export());
    }

}
