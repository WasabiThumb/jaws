import {Lobby, LobbyChat, LobbyCode} from "../../struct/lobby";
import {ClientUser} from "./user";
import {ChatMessage, ChatSender} from "../../struct/chat";
import {User} from "../../struct/user";
import JawsClient from "../client";
import Packet from "../../packet/packet";
import PacketInLobbyChat from "../../packet/impl/in/lobbyChat";
import {ClientRemoteUser} from "./user/remote";
import {EncryptKey} from "../../crypto/struct";
import PacketInLobbyRequestChatHistory from "../../packet/impl/in/lobbyRequestChatHistory";
import PacketOutLobbyChatHistory from "../../packet/impl/out/lobbyChatHistory";
import UUID from "../../util/uuid";
import PacketOutLobbyChat from "../../packet/impl/out/lobbyChat";

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

    getUser(id: UUID): ClientUser | null {
        let user: ClientUser;
        for (let i=0; i < this.users.length; i++) {
            user = this.users[i];
            if (id.compare(user.id) === 0) return user;
        }
        return null;
    }

}

type HistoryEntry = { message: ChatMessage, token: number };
type Callback = ((event: any) => void);
export class ClientLobbyChat implements LobbyChat {

    readonly lobby: ClientLobby;
    readonly maxMessageLength: number = 260; // TODO: Make dynamic
    private _fetchedInitialHistory: boolean = false;
    private _history: HistoryEntry[] = [];
    private _historyTransactionHead: number = 0;
    private _historyActiveRefresh: Promise<void> | null = null;
    private _callbacks: { [k: string]: Callback[] } = {};
    constructor(lobby: ClientLobby) {
        this.lobby = lobby;
    }

    getMessageHistory(): ChatMessage[] {
        if (!this._fetchedInitialHistory) {
            this.refresh(true).catch(console.error);
        }
        return this._history.map<ChatMessage>((entry) => entry.message);
    }

    refresh(initial?: boolean): Promise<void> {
        if (!!this._historyActiveRefresh) return this._historyActiveRefresh;
        this._fetchedInitialHistory = true;

        const transaction: number = this._historyTransactionHead;
        const lobby: ClientLobby = this.lobby;
        const client: JawsClient = lobby.client;
        const me = this;
        const prom: Promise<void> = new Promise<void>(async (res, rej) => {
            const received = await client.awaitPacket(PacketOutLobbyChatHistory);
            if (received.transaction !== transaction) {
                rej("Transaction mismatch");
                return;
            }
            let history: HistoryEntry[] = new Array(received.messages.length);
            let len: number = 0;
            let message: ChatMessage;
            let id: UUID;
            let user: ClientUser | null;
            for (let i = 0; i < received.messages.length; i++) {
                id = received.messageSenders[i];
                user = lobby.getUser(id);
                if (!user) continue;
                message = received.messages[i];
                message.sender = user;
                if (!!initial && id.compare(lobby.client.user.id) !== 0) this._onNewMessage(message);
                history[len++] = { message, token: received.tokens[i] };
            }
            if (len < history.length) history = history.slice(0, len);
            me._history = history;
            me._historyActiveRefresh = null;
        });

        this._historyActiveRefresh = prom;
        prom.then(() => {
            me._historyActiveRefresh = null;
        }).catch(() => {
            me._historyActiveRefresh = null;
        });

        const packet = new PacketInLobbyRequestChatHistory();
        packet.transaction = transaction;
        client.sendPacket(packet);

        return prom;
    }

    broadcast(content: string): ChatMessage {
        const cm: ChatMessage = new ChatMessage.Basic(content);
        cm.sender = this.lobby.client.user;
        this._addToHistory(cm);
        this._onNewMessage(cm);

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
            ret.sender = this.lobby.client.user;
            if (!knownKey) {
                this._addToHistory(ret);
                this._onNewMessage(ret);
            }
            const packet = new PacketInLobbyChat();
            packet.message = ret;
            this.lobby.client.sendPacket(packet);
        } else {
            ret = ChatMessage.Whisper.createFacade(receiver.id, content);
            ret.sender = this.lobby.client.user;
            const me = this;
            (receiver as ClientRemoteUser).fetchPublicKey(this.lobby.client).then((k) => {
                me.whisper(receiver, content, k);
            }).catch(console.error);

            this._addToHistory(ret);
            this._onNewMessage(ret);
        }
        return ret;
    }

    handlePacket(packet: Packet): void {
        if (packet instanceof PacketOutLobbyChat) {
            const { message, senderID, idempotency } = packet;
            if (message.type === ChatMessage.Type.SYSTEM) {
                message.sender = ChatSender.SYSTEM;
            } else {
                const user: ClientUser | null = this.lobby.getUser(senderID);
                if (!user) return;
                message.sender = user;
            }
            this._addToHistory(message, idempotency);
            this._onNewMessage(message);
        }
    }

    private _onNewMessage(message: ChatMessage): void {
        const { encryption, key } = this.lobby.client.user;
        const event: LobbyChat.MessageEvent = {
            type: "message",
            message,
            get content(): string {
                return message.getMessage(encryption, key);
            }
        };
        this._fireCallback("message", event);
    }

    private _addToHistory(message: ChatMessage, token?: number): void {
        if (typeof token !== "number") {
            this._history.push({ message, token: -1 });
            return;
        }

        const entry: HistoryEntry = { message, token };
        const hLen: number = this._history.length;
        if (hLen === 0) {
            this._history = [ entry ];
            return;
        }

        const lastIndex = hLen - 1;
        let insertAfter = lastIndex;
        let curToken: number;
        while ((curToken = this._history[insertAfter].token) >= token) {
            if (curToken === token) return;
            insertAfter--;
            if (insertAfter < 0) break;
        }

        if (insertAfter === lastIndex) {
            this._history.push(entry);
        } else {
            this._history.splice(insertAfter + 1, 0, entry);
        }
    }

    private _fireCallback(id: keyof LobbyChat.EventMap, event: LobbyChat.Event) {
        let callbacks: Callback[] = this._callbacks[id];
        if (!callbacks) return;
        for (let cb of callbacks) {
            cb(event);
        }
    }

    on<T extends keyof LobbyChat.EventMap, E extends LobbyChat.EventMap[T] & LobbyChat.Event>(event: T, cb: (event: E) => void) {
        let callbacks: Callback[] = this._callbacks[event];
        if (!callbacks) callbacks = [];
        callbacks.push(cb as unknown as Callback);
        this._callbacks[event] = callbacks;
    }

    off(event: keyof LobbyChat.EventMap, cb: (event: LobbyChat.Event) => void): void {
        let callbacks: Callback[] | undefined = this._callbacks[event];
        if (!callbacks) return;
        let idx = callbacks.indexOf(cb as unknown as Callback);
        if (idx === -1) return;
        callbacks.splice(idx, 1);
    }

}
