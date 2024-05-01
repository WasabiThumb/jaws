import {User} from "./user";
import nacl from "tweetnacl";
import {ChatMessage} from "./chat";

export namespace LobbyChat {

    export type MessageEvent = { type: "message", message: ChatMessage, content: string };
    export type Event = MessageEvent;
    export type EventMap = {
        "message": EventMap;
    };

}

export interface LobbyChat {

    readonly lobby: Lobby;

    readonly maxMessageLength: number;

    getMessageHistory(): ChatMessage[];

    broadcast(content: string): ChatMessage;

    whisper(receiver: User, content: string): ChatMessage;

    refresh(): Promise<void>;

    on<T extends keyof LobbyChat.EventMap, E extends LobbyChat.EventMap[T] & LobbyChat.Event>(event: T, cb: (event: E) => void): void;

    off(event: keyof LobbyChat.EventMap, cb: ((event: LobbyChat.Event) => void)): void;

}

export type Lobby = {

    readonly code: string;

    readonly chat: LobbyChat;

    name: string;

    publicMatchmaking: boolean;

    users: User[];

    owner: User | null;

};

// Lobby Code
const LEGAL_CHARS: string = "23456789ABCDEFGHJKMNPQRSTUVWXY01";
const C2I: Uint8Array = (() => {
    const arr: Uint8Array = new Uint8Array(74);
    arr.fill(-1);

    let c: number;
    for (let i=0; i < LEGAL_CHARS.length; i++) {
        c = LEGAL_CHARS.charCodeAt(i);
        arr[c - 48] = i;
        if (c < 50) {
            if (c == 48) {
                arr[31] = i; arr[63] = i;
            } else {
                arr[25] = i; arr[28] = i; arr[57] = i; arr[60] = i;
            }
        } else if (c > 57) {
            arr[c - 16] = i;
        }
    }
    return arr;
})();
export const LobbyCode = new class {

    generate(): string {
        const bytes: Uint8Array = nacl.randomBytes(4);
        const num: number = ((bytes[0] & 63) << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3];
        return this.fromInt(num);
    }

    toInt(s: string): number | -1 {
        if (s.length != 6) return -1;
        let ret: number = 0;
        let idx: number;
        for (let i=0; i < 6; i++) {
            idx = s.charCodeAt(i) - 48;
            if (idx < 0 || idx >= 74) return -1;
            idx = C2I[idx];
            if (idx === -1) return -1;
            ret <<= 5;
            ret |= idx;
        }
        return ret;
    }

    fromInt(n: number): string {
        let chars: string[] = new Array(6);
        for (let i=0; i < 6; i++) {
            chars[5 - i] = LEGAL_CHARS[n & 31];
            n >>= 5;
        }
        return chars.shift()!.concat(...chars);
    }

};
