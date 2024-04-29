import {ClientFacetAdapter} from "../facet";
import JawsClient from "../../client";
import {User} from "../../../struct/user";
import Packet from "../../../packet/packet";
import ClientLobby from "../lobby";
import PacketOutLobbyData from "../../../packet/impl/out/lobbyData";
import {ClientLocalUser} from "../user/local";
import {ClientUser} from "../user";
import UUID from "../../../util/uuid";
import {ClientRemoteUser} from "../user/remote";
import {LobbyCode} from "../../../struct/lobby";
import PacketInLobbyJoin from "../../../packet/impl/in/lobbyJoin";
import PacketInLobbyCreate from "../../../packet/impl/in/lobbyCreate";
import {FriendlyName} from "../../../util/string";
import {withTimeout} from "../../../util/async";

export default class LobbyClientFacet extends ClientFacetAdapter {

    private _transactionCallbacks: { [ id: number ]: ((lobby: ClientLobby) => void) } = {};
    private _transactionHead: number = 0;
    private _client: JawsClient | null = null;

    onInit(ctx: JawsClient) {
        this._client = ctx;
    }

    create(name: string): Promise<ClientLobby> {
        if (!FriendlyName.isLegal(name)) return Promise.reject("Lobby name is invalid");
        const transaction: number = this._transactionHead++;
        const ret = this._awaitTransaction(transaction);
        const packet = new PacketInLobbyCreate();
        packet.transaction = transaction;
        packet.name = name;
        this._client!.sendPacket(packet);
        return ret;
    }

    join(id: string | number): Promise<ClientLobby> {
        if (typeof id === "string") id = LobbyCode.toInt(id);
        const transaction: number = this._transactionHead++;
        const ret = this._awaitTransaction(transaction);
        const packet = new PacketInLobbyJoin();
        packet.transaction = transaction;
        packet.intLobbyCode = id;
        this._client!.sendPacket(packet);
        return ret;
    }

    onMessage(ctx: JawsClient, user: User, received: Packet): void {
        if (received instanceof PacketOutLobbyData) {
            this._processDataPacket(received, ctx.user);
        }
    }

    onDisconnect(ctx: JawsClient, user: User): void {
        ctx.user.lobby = null;
    }

    private _awaitTransaction(id: number): Promise<ClientLobby> {
        const me = this;
        return withTimeout(new Promise<ClientLobby>((res) => {
            me._transactionCallbacks[id] = res;
        }));
    }

    private _processDataPacket(packet: PacketOutLobbyData, user: ClientLocalUser) {
        let ob: ClientLobby;
        if (packet.lobbyCode === user.lobby?.intCode) {
            ob = user.lobby!;
        } else {
            ob = new ClientLobby(packet.lobbyCode);
        }
        ob.publicMatchmaking = packet.isPublic;
        ob.name = packet.lobbyName;

        let users: ClientUser[] = new Array(packet.userCount);
        let id: UUID;
        let name: string;
        for (let i=0; i < packet.userCount; i++) {
            id = packet.userIDs[i];
            if (id.compare(user.id) === 0) {
                users[i] = user;
                continue;
            }
            name = packet.userNames[i];
            users[i] = new ClientRemoteUser(id, name);
        }

        ob.users = users;
        ob.owner = packet.ownerIndex < users.length ? users[packet.ownerIndex] : null;
        user.lobby = ob;
        const cb = this._transactionCallbacks[packet.transaction];
        if (!!cb) {
            cb(ob);
            delete this._transactionCallbacks[packet.transaction];
        }
    }

}
