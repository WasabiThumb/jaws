import Packet from "../../packet";
import {PacketData} from "../../data";
import {LobbyCode} from "../../../struct/lobby";

export default class PacketInLobbyJoin extends Packet {

    transaction: number = 0;
    intLobbyCode: number = 0;
    constructor() {
        super(6);
    }

    get lobbyCode(): string {
        return LobbyCode.fromInt(this.intLobbyCode);
    }

    set lobbyCode(code: string) {
        this.intLobbyCode = LobbyCode.toInt(code);
    }

    getElementCount(): number {
        return 2;
    }

    read(dat: PacketData): void {
        const transaction: number = dat.readUInt8();
        const lobbyCode: number = dat.readInt32();
        this.transaction = transaction;
        this.intLobbyCode = lobbyCode;
    }

    write(dat: PacketData): void {
        dat.writeUInt8(this.transaction);
        dat.writeInt32(this.intLobbyCode);
    }

}
