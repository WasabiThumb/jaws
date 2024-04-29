import {EncryptHandler, EncryptKey} from "../struct";

class NoneEncryptKey implements EncryptKey {

    export(): Uint8Array {
        return new Uint8Array(0);
    }

}
const NONE_KEY: NoneEncryptKey = new NoneEncryptKey();

export default class NoneEncryptHandler implements EncryptHandler {

    readonly identifier: number = 0;
    readonly keySize: number = 0;

    generateLocalKey(): EncryptKey {
        return NONE_KEY;
    }

    importRemoteKey(data: Uint8Array): EncryptKey {
        return NONE_KEY;
    }

    encrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        return data;
    }

    decrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        return data;
    }

}
