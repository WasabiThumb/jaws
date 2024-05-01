import UUID from "../util/uuid";
import {EncryptHandler, EncryptKey} from "../crypto/struct";
import Long from "long";
import * as utf8 from "@stablelib/utf8";
import {User} from "./user";

enum ChatMessageType {
    SYSTEM,
    BASIC,
    WHISPER
}

export interface ChatSender<U extends boolean = boolean, S extends boolean = boolean> {

    readonly name: string;

    isUser(): U;

    isSystem(): S;

}
export type UserChatSender = ChatSender<true, false> & { id: UUID, key: EncryptKey | null };

class SystemChatSender implements ChatSender<false, true> {

    readonly name: string = "SYSTEM";

    isSystem(): true {
        return true;
    }

    isUser(): false {
        return false;
    }

}

export namespace ChatSender {

    export const SYSTEM: ChatSender<false, true> = new SystemChatSender();

}

export interface ChatMessage {

    readonly type: ChatMessageType;

    sender: ChatSender;

    readonly senderID: UUID;

    readonly rawData: Uint8Array;

    getMessage(method?: EncryptHandler, receiverKey?: EncryptKey): string;

    isWhisper(): boolean;

    isLikelyNotLongerThan(len: number): boolean;

}

abstract class AbstractChatMessage implements ChatMessage {

    abstract readonly type: ChatMessageType;
    protected _sender: ChatSender | null = null;
    readonly rawData: Uint8Array;
    protected constructor(rawData: Uint8Array | string) {
        this.rawData = typeof rawData === "string" ? utf8.encode(rawData) : rawData;
    }

    get sender(): ChatSender {
        if (!this._sender) throw new Error("Chat sender has not been set");
        return this._sender!;
    }

    set sender(sender: ChatSender) {
        if (!!this._sender) throw new Error("Cannot set chat sender more than once");
        this._sender = sender;
    }

    get senderID(): UUID {
        const { sender } = this;
        if (sender.isSystem()) return new UUID(Long.ZERO, Long.ZERO);
        return (sender as unknown as { id: UUID }).id;
    }

    getMessage(method?: EncryptHandler, receiverKey?: EncryptKey): string {
        return utf8.decode(this.rawData);
    }

    isLikelyNotLongerThan(len: number): boolean {
        return len >= this.getMessage().length;
    }

    isWhisper(): boolean {
        return false;
    }

}

class SystemChatMessage extends AbstractChatMessage {

    readonly type: ChatMessageType = ChatMessageType.SYSTEM;
    constructor(rawData: Uint8Array | string) {
        super(rawData);
    }

}

abstract class AbstractUserChatMessage extends AbstractChatMessage {

    abstract readonly type: ChatMessageType;

    get sender(): UserChatSender {
        if (!this._sender) throw new Error("Chat sender has not been set");
        return this._sender! as UserChatSender;
    }

    set sender(sender: ChatSender) {
        if (!!this._sender) throw new Error("Cannot set chat sender more than once");
        if (!(sender as unknown as { id: any })) throw new Error("Cannot set message sender to a non-user");
        this._sender = sender;
    }

}

class BasicChatMessage extends AbstractUserChatMessage {

    readonly type: ChatMessageType = ChatMessageType.BASIC;
    constructor(rawData: Uint8Array | string) {
        super(rawData);
    }

}

class WhisperChatMessage extends AbstractUserChatMessage {

    static create(sender: User & { encryption: EncryptHandler }, receiver: User, content: string) {
        if (!receiver.key) throw new Error("Receiver key not set");
        let dat: Uint8Array = utf8.encode(content);
        dat = sender.encryption.encrypt(dat, sender.key!, receiver.key);
        const ret: WhisperChatMessage = new WhisperChatMessage(receiver.id, dat);
        ret._unencrypted = content;
        return ret;
    }

    static createFacade(receiverID: UUID, content: string) {
        const ret: WhisperChatMessage = new WhisperChatMessage(receiverID, content);
        ret._unencrypted = content;
        return ret;
    }

    readonly type: ChatMessageType = ChatMessageType.WHISPER;
    readonly receiverID: UUID;
    private _unencrypted: string | -1 = -1;
    constructor(receiverID: UUID, rawData: Uint8Array | string) {
        super(rawData);
        this.receiverID = receiverID;
    }

    getMessage(method?: EncryptHandler, receiverKey?: EncryptKey): string {
        if (this._unencrypted !== -1) return this._unencrypted;
        if ((!method) || (!receiverKey)) throw new Error("Cannot get whisper message content without encryption key");
        const senderKey: EncryptKey | null = this.sender.key;
        if (!senderKey) throw new Error("Whisper message sender key is not set");
        const data: Uint8Array = method.decrypt(this.rawData, senderKey, receiverKey);
        const ret: string = utf8.decode(data);
        this._unencrypted = ret;
        return ret;
    }

    isLikelyNotLongerThan(len: number): boolean {
        return ((len << 2) + 40) >= this.rawData.length;
    }

}

export namespace ChatMessage {

    export const PLACEHOLDER: ChatMessage = new SystemChatMessage("System Message");

    export type Type = ChatMessageType;
    export const Type = ChatMessageType;

    export type Basic = BasicChatMessage;
    export const Basic = BasicChatMessage;

    export type System = SystemChatMessage;
    export const System = SystemChatMessage;

    export type Whisper = WhisperChatMessage;
    export const Whisper = WhisperChatMessage;

}
