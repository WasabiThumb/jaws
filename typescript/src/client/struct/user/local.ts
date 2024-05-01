import Packet from "../../../packet/packet";
import UUID from "../../../util/uuid";
import {EncryptHandler, EncryptKey} from "../../../crypto/struct";
import {ByteOrder} from "../../../util/data";
import {CompressHandler} from "../../../compress/struct";
import {Lobby} from "../../../struct/lobby";
import {ClientUser} from "../user";
import ClientLobby from "../lobby";
import {ChatReader} from "../../../struct/chat";

type Connection = { sendPacket(packet: Packet): void };

export class ClientLocalUser implements ClientUser, ChatReader {

    readonly connection: Connection;
    readonly id: UUID;
    readonly name: string;
    readonly key: EncryptKey;
    readonly serverKey: EncryptKey;
    readonly order: ByteOrder;
    readonly encryption: EncryptHandler;
    readonly compression: CompressHandler;
    lobby: ClientLobby | null = null;

    constructor(connection: Connection, id: UUID, name: string, key: EncryptKey, serverKey: EncryptKey, order: ByteOrder, encryption: EncryptHandler, compression: CompressHandler) {
        this.connection = connection;
        this.id = id;
        this.name = name;
        this.key = key;
        this.serverKey = serverKey;
        this.order = order;
        this.encryption = encryption;
        this.compression = compression;
    }

    wrapData(data: Uint8Array): Uint8Array {
        const {compression, encryption} = this;
        data = encryption.encrypt(data, this.key, this.serverKey);
        data = compression.compress(data);
        return data;
    }

    unwrapData(data: Uint8Array): Uint8Array {
        const {compression, encryption} = this;
        data = compression.decompress(data);
        data = encryption.decrypt(data, this.serverKey, this.key);
        return data;
    }

    sendPacket(packet: Packet): void {
        this.connection.sendPacket(packet);
    }

    isSystem(): false {
        return false;
    }

    isUser(): true {
        return true;
    }

}
