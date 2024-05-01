import Packet from "../../packet";
import {PacketData} from "../../data";
import UUID from "../../../util/uuid";
import {ChatMessage} from "../../../struct/chat";
import PacketOutLobbyChat from "./lobbyChat";

export default class PacketOutLobbyChatHistory extends Packet {

    transaction: number = 0;
    tokens: number[] = [];
    messageSenders: UUID[] = [];
    messages: ChatMessage[] = [];
    constructor() {
        super(13);
    }

    getElementCount(): number {
        return 0;
    }

    read(dat: PacketData): void {
        this.transaction = dat.readInt32();

        const count: number = dat.readUInt16();
        this.tokens = new Array(count);
        this.messageSenders = new Array(count);
        this.messages = new Array(count);

        const single = new PacketOutLobbyChat();
        for (let i=0; i < count; i++) {
            single.read(dat);
            this.tokens[i] = single.idempotency;
            this.messageSenders[i] = single.senderID;
            this.messages[i] = single.message;
        }
    }

    write(dat: PacketData): void {
        dat.writeInt32(this.transaction);
        dat.writeUInt16(this.messages.length);

        const single = new PacketOutLobbyChat();
        for (let i=0; i < this.messages.length; i++) {
            single.message = this.messages[i];
            single.idempotency = this.tokens[i];
            single.write(dat);
        }
    }

}
