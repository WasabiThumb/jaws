import UUID from "../util/uuid";
import {Lobby} from "./lobby";

export type User = {

    readonly id: UUID;

    readonly name: string;

    lobby: Lobby | null;

}
