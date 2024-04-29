import Packet from "../../packet";
import {PacketData} from "../../data";
import UUID from "../../../util/uuid";
import Long from "long";
import {FriendlyName, StringUtil} from "../../../util/string";
import * as utf8 from "@stablelib/utf8";
import DataViewPacketData from "../../data/view";


export default class PacketOutLobbyData extends Packet {

    transaction: number = 0;
    lobbyCode: number = 0;
    lobbyName: string = "Lobby";
    userCount: number = 0;
    userIDs: UUID[] = [];
    userNames: string[] = [];
    ownerIndex: number = 0;
    isPublic: boolean = false;
    constructor() {
        super(7);
    }

    getElementCount(): number {
        return 0;
    }

    read(dat: PacketData): void {
        const transaction: number = dat.readUInt8();
        const lobbyCode: number = dat.readInt32();
        const userCount: number = dat.readUInt16();
        const ownerIndex: number = dat.readUInt16();

        const userIDs: UUID[] = new Array(userCount);
        let hi: Long, lo: Long;
        for (let i=0; i < userCount; i++) {
            hi = dat.readInt64();
            lo = dat.readInt64();
            userIDs[i] = new UUID(hi, lo);
        }

        const isPublic: boolean = dat.readBoolean();
        const nameLens: number[] = new Array(userCount);
        for (let i=0; i < userCount; i++) {
            nameLens[i] = dat.readBoolean() ? 16 : 0;
        }

        let b: number = 0;
        let nb: number;
        for (let i=0; i < userCount; i++) {
            if (i & 1) {
                nb = b & 15;
            } else {
                b = dat.readUInt8();
                nb = b >> 4;
            }
            nameLens[i] |= nb;
        }

        const userNames: string[] = new Array(userCount);
        for (let i=0; i < userCount; i++) {
            nb = nameLens[i] + 1;
            nb = (nb << 2) - nb;
            userNames[i] = StringUtil.decodePossiblyNullTerm(dat.readLiteral(nb));
        }

        nb = (dat.readUInt8() & 0x1F) + 1;
        const bytes: Uint8Array = dat.readRemainingUpTo((nb << 2) - nb);
        if (!FriendlyName.isLegal(bytes)) throw new Error("Illegal lobby name");

        this.transaction = transaction;
        this.lobbyCode = lobbyCode;
        this.lobbyName = utf8.decode(bytes);
        this.userCount = userCount;
        this.userIDs = userIDs;
        this.userNames = userNames;
        this.ownerIndex = ownerIndex;
        this.isPublic = isPublic;
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.transaction);
        dat.writeInt32(this.lobbyCode);
        dat.writeUInt16(this.userCount);
        dat.writeUInt16(this.ownerIndex);

        let uuid: UUID;
        const nameLenLo: number[] = new Array(this.userCount);
        const nameLenHi: boolean[] = new Array(this.userCount);
        for (let i = 0; i < this.userCount; i++) {
            let nameLen: number = this.userNames[i].length - 1;
            nameLenHi[i] = !!(nameLen & 16);
            nameLenLo[i] = nameLen & 15;
            uuid = this.userIDs[i];
            dat.writeInt64(uuid.mostSignificantBits);
            dat.writeInt64(uuid.leastSignificantBits);
        }

        dat.writeBoolean(this.isPublic);
        for (let b of nameLenHi) dat.writeBoolean(b);

        const pairs: number = (this.userCount + 1) >> 1;
        let z: number = 0;
        for (let i=0; i < pairs; i++) {
            let b: number = nameLenLo[i] << 4;
            z++;
            if (z < this.userCount) {
                dat.writeUInt8(b | nameLenLo[z]);
            } else {
                dat.writeUInt8(b);
                break;
            }
            z++;
        }

        let n: string;
        let maxBytes: number;
        for (let i=0; i < this.userCount; i++) {
            n = this.userNames[i];
            maxBytes = n.length;
            maxBytes = (maxBytes << 2) - maxBytes;

            const bv: Uint8Array = utf8.encode(n);
            dat.writeLiteral(bv);
            if (bv.length < maxBytes) {
                dat.writeLiteral(new Uint8Array(maxBytes - bv.length));
            }
        }

        dat.writeUInt8((this.lobbyName.length - 1) & 0x1F);
        dat.writeLiteral(utf8.encode(this.lobbyName));
    }

}
