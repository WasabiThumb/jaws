import {Lobby, LobbyCode} from "../../struct/lobby";
import {ClientUser} from "./user";

export default class ClientLobby implements Lobby {

    readonly intCode: number;
    name: string = "Lobby";
    owner: ClientUser | null = null;
    publicMatchmaking: boolean = false;
    users: ClientUser[] = [];

    constructor(intCode: number) {
        this.intCode = intCode;
    }

    get code(): string {
        return LobbyCode.fromInt(this.intCode);
    }

}
