import UUID from "../../../util/uuid";
import {ClientUser} from "../user";
import {Lobby} from "../../../struct/lobby";
import {EncryptKey} from "../../../crypto/struct";
import JawsClient from "../../client";
import PacketInLobbyRequestPeerKey from "../../../packet/impl/in/lobbyRequestPeerKey";
import PacketOutLobbyPeerKey from "../../../packet/impl/out/lobbyPeerKey";

let KEY_REQUEST_TRANSACTION_HEAD = 0;
export class ClientRemoteUser implements ClientUser {

    readonly id: UUID;
    readonly name: string;
    lobby: Lobby | null = null;
    key: EncryptKey | null = null;

    constructor(id: UUID, name: string) {
        this.id = id;
        this.name = name;
    }

    isSystem(): false {
        return false;
    }

    isUser(): true {
        return true;
    }

    fetchPublicKey(client: JawsClient): Promise<EncryptKey> {
        if (!!this.key) return Promise.resolve(this.key!);
        const transaction: number = KEY_REQUEST_TRANSACTION_HEAD++;
        const request = new PacketInLobbyRequestPeerKey();
        request.transaction = transaction;
        request.peerID = this.id;

        const me = this;
        const ret: Promise<EncryptKey> = client.awaitPacket(PacketOutLobbyPeerKey).then((p: PacketOutLobbyPeerKey) => {
            if (p.transaction !== transaction) throw new Error("Transaction mismatch");
            const key: EncryptKey = client.user.encryption.importRemoteKey(p.keyData);
            me.key = key;
            return key;
        });

        client.sendPacket(request);
        return ret;
    }

}
