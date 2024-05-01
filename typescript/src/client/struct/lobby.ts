import {Lobby, LobbyChat, LobbyCode} from "../../struct/lobby";
import {ClientUser} from "./user";
import {ChatMessage} from "../../struct/chat";
import {User} from "../../struct/user";
import JawsClient from "../client";
import Packet from "../../packet/packet";
import PacketInLobbyChat from "../../packet/impl/in/lobbyChat";
import {ClientRemoteUser} from "./user/remote";
import {EncryptKey} from "../../crypto/struct";

export default class ClientLobby implements Lobby {

    readonly client: JawsClient;
    readonly intCode: number;
    readonly chat: ClientLobbyChat;
    name: string = "Lobby";
    owner: ClientUser | null = null;
    publicMatchmaking: boolean = false;
    users: ClientUser[] = [];

    constructor(client: JawsClient, intCode: number) {
        this.client = client;
        this.intCode = intCode;
        this.chat = new ClientLobbyChat(this);
    }

    get code(): string {
        return LobbyCode.fromInt(this.intCode);
    }

}

export class ClientLobbyChat implements LobbyChat {

    readonly lobby: ClientLobby;
    readonly maxMessageLength: number = 260; // TODO: Make dynamic
    constructor(lobby: ClientLobby) {
        this.lobby = lobby;
    }

    getMessageHistory(): ChatMessage[] {
        return [];
    }

    refresh(): Promise<void> {
        // TODO: Fetch history
        return Promise.resolve(undefined);
    }

    broadcast(content: string): ChatMessage {
        const cm: ChatMessage = new ChatMessage.Basic(content);
        cm.sender = this.lobby.client.user;
        // TODO: Add to history

        const packet = new PacketInLobbyChat();
        packet.message = cm;
        this.lobby.client.sendPacket(packet);

        return cm;
    }

    whisper(receiver: User, content: string, knownKey?: EncryptKey): ChatMessage {
        let { key } = receiver;
        if (!!knownKey) key = knownKey;
        let ret: ChatMessage;
        if (!!key) {
            ret = ChatMessage.Whisper.create(this.lobby.client.user, receiver, content);
            // TODO: Add to history

            const packet = new PacketInLobbyChat();
            packet.message = ret;
            this.lobby.client.sendPacket(packet);
        } else {
            ret = ChatMessage.Whisper.createFacade(receiver.id, content);
            const me = this;
            (receiver as ClientRemoteUser).fetchPublicKey(this.lobby.client).then((k) => {
                me.whisper(receiver, content, k);
            }).catch(console.error);
        }
        return ret;
    }

    handlePacket(packet: Packet): void {

    }

}
