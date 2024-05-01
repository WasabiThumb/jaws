import UUID from "../util/uuid";
import {Lobby} from "./lobby";
import {UserChatSender} from "./chat";
import {EncryptKey} from "../crypto/struct";

export type User = UserChatSender & {

    readonly id: UUID;

    readonly name: string;

    lobby: Lobby | null;

    key: EncryptKey | null;

}
