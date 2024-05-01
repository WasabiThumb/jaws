import Packet from "../../packet";
import Long from "long";
import UUID from "../../../util/uuid";
import {ChatMessage} from "../../../struct/chat";
import {PacketData} from "../../data";

export default class PacketOutLobbyChat extends Packet {

    static readonly CURSED_FLAG: Long = new Long(0, 0x40000000, false);

    senderID: UUID = new UUID(Long.ZERO, Long.ZERO);
    message: ChatMessage = ChatMessage.PLACEHOLDER;
    idempotency: number = 0;
    constructor() {
        super(11);
    }

    getElementCount(): number {
        return 7;
    }

    read(dat: PacketData): void {
        const seq: number = dat.readInt32();

        const mostSig: Long = dat.readInt64();
        let type: ChatMessage.Type;
        const receiver: Long[] = new Array(2);

        if (mostSig.eq(Long.ZERO)) {
            type = ChatMessage.Type.SYSTEM;
            this.senderID = new UUID(Long.ZERO, Long.ZERO);
        } else {
            let leastSig: Long = dat.readInt64();
            if (leastSig.and(PacketOutLobbyChat.CURSED_FLAG).neq(Long.ZERO)) {
                receiver[0] = dat.readInt64();
                receiver[1] = dat.readInt64();
                type = ChatMessage.Type.WHISPER;
                leastSig = leastSig.xor(PacketOutLobbyChat.CURSED_FLAG);
            } else {
                type = ChatMessage.Type.BASIC;
            }
            this.senderID = new UUID(mostSig, leastSig);
        }

        const len: number = dat.readUInt16();
        const raw: Uint8Array = dat.readLiteral(len);
        switch (type) {
            case ChatMessage.Type.BASIC:
                this.message = new ChatMessage.Basic(raw);
                break;
            case ChatMessage.Type.WHISPER:
                this.message = new ChatMessage.Whisper(new UUID(receiver[0], receiver[1]), raw);
                break
            case ChatMessage.Type.SYSTEM:
                this.message = new ChatMessage.System(raw);
                break;
        }

        this.idempotency = seq;
    }

    write(dat: PacketData): void {
        dat.writeInt32(this.idempotency);
        const senderID: UUID = this.message.senderID;
        dat.writeInt64(senderID.mostSignificantBits);

        if (this.message.type !== ChatMessage.Type.SYSTEM) {
            let leastSig: Long = senderID.leastSignificantBits;
            if (this.message.type === ChatMessage.Type.WHISPER) {
                const { receiverID } = this.message as ChatMessage.Whisper;
                dat.writeInt64(leastSig.or(PacketOutLobbyChat.CURSED_FLAG));
                dat.writeInt64(receiverID.mostSignificantBits);
                dat.writeInt64(receiverID.leastSignificantBits);
            } else {
                dat.writeInt64(leastSig);
            }
        }

        const data: Uint8Array = this.message.rawData;
        dat.writeUInt16(data.length);
        dat.writeLiteral(data);
    }

}
