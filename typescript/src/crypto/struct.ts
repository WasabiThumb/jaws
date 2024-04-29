
export interface EncryptKey {

    export(): Uint8Array;

}

export interface EncryptHandler {

    readonly identifier: number;
    readonly keySize: number;

    generateLocalKey(): EncryptKey;

    importRemoteKey(data: Uint8Array): EncryptKey;

    encrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array;

    decrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array;

}
