import UUID from "../../../util/uuid";
import {ClientUser} from "../user";
import {Lobby} from "../../../struct/lobby";

export class ClientRemoteUser implements ClientUser {

    readonly id: UUID;
    readonly name: string;
    lobby: Lobby | null = null;

    constructor(id: UUID, name: string) {
        this.id = id;
        this.name = name;
    }

}
