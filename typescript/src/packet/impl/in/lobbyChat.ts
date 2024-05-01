import Packet from "../../packet";
import {PacketData} from "../../data";
import {ChatMessage} from "../../../struct/chat";
import Long from "long";
import UUID from "../../../util/uuid";

export default class PacketInLobbyChat extends Packet {

    message: ChatMessage = ChatMessage.PLACEHOLDER;
    constructor() {
        super(10);
    }

    getElementCount(): number {
        return 5;
    }

    read(dat: PacketData): void {
        let len: number = dat.readUInt8();
        const fetchId: boolean = (len & 128) != 0;
        if (fetchId) len &= 127;
        if (len == 127) len = dat.readUInt16();

        const bits: Long[] = new Array(2);
        if (fetchId) {
            bits[0] = dat.readInt64();
            bits[1] = dat.readInt64();
            len += 41;
        } else {
            len++;
        }

        const data: Uint8Array = dat.readRemainingUpTo(len);
        if (fetchId) {
            this.message = new ChatMessage.Whisper(new UUID(bits[0], bits[1]), data);
        } else {
            this.message = new ChatMessage.Basic(data);
        }
    }

    write(dat: PacketData): void {
        if (this.message.type === ChatMessage.Type.SYSTEM)
            throw new Error(`Cannot write a system message (${this.message})`);

        const { rawData } = this.message;
        let len: number = rawData.length;
        if (len === 0) throw new Error("Message is empty");
        const writeId: boolean = this.message.type === ChatMessage.Type.WHISPER;
        if (writeId) {
            len = Math.max(len - 41, 0);
        } else {
            len--;
        }
        if (len > 65535) throw new Error("Message is too long");

        const writeFull: boolean = len > 126;
        let lenLo: number = writeFull ? 127 : len;
        if (writeId) lenLo |= 128;
        dat.writeUInt8(lenLo);
        if (writeFull) dat.writeUInt16(lenLo);
        if (writeId) {
            const { receiverID } = this.message as ChatMessage.Whisper;
            dat.writeInt64(receiverID.mostSignificantBits);
            dat.writeInt64(receiverID.leastSignificantBits);
        }
        dat.writeLiteral(rawData);
    }

}
