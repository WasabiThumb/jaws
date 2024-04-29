import {EncryptHandler, EncryptKey} from "../struct";
import nacl from "tweetnacl";

interface SodiumEncryptKey extends EncryptKey {

    readonly publicKey: Uint8Array;

}

class SodiumLocalEncryptKey implements SodiumEncryptKey {

    readonly publicKey: Uint8Array;
    readonly secretKey: Uint8Array;
    constructor(publicKey: Uint8Array, secretKey: Uint8Array) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }

    export(): Uint8Array {
        return this.publicKey;
    }

}

class SodiumRemoteEncryptKey implements SodiumEncryptKey {

    readonly publicKey: Uint8Array;
    constructor(publicKey: Uint8Array) {
        this.publicKey = publicKey;
    }

    export(): Uint8Array {
        return this.publicKey;
    }

}

export default class SodiumEncryptHandler implements EncryptHandler {

    readonly identifier: number = 2;
    readonly keySize: number = 32;

    generateLocalKey(): EncryptKey {
        const kp = nacl.box.keyPair();
        return new SodiumLocalEncryptKey(kp.publicKey, kp.secretKey);
    }

    importRemoteKey(data: Uint8Array): EncryptKey {
        if (data.length !== 32) throw new Error("Public key is not 32 bytes (got " + data.length + ")");
        return new SodiumRemoteEncryptKey(data);
    }

    encrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        const sender: Uint8Array = (senderKey as SodiumLocalEncryptKey).secretKey;
        const receiver: Uint8Array = (receiverKey as SodiumEncryptKey).publicKey;

        const nonce: Uint8Array = nacl.randomBytes(24);
        const box: Uint8Array = nacl.box(data, nonce, receiver, sender);

        const ret: Uint8Array = new Uint8Array(nonce.length + box.length);
        ret.set(nonce, 0);
        ret.set(box, nonce.length);
        return ret;
    }

    decrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        if (data.length < 24) throw new Error("Data is less than 24 bytes long");

        const nonce: Uint8Array = data.subarray(0, 24);
        data = data.subarray(24);

        const sender: Uint8Array = (senderKey as SodiumEncryptKey).publicKey;
        const receiver: Uint8Array = (receiverKey as SodiumLocalEncryptKey).secretKey;

        const ret = nacl.box.open(data, nonce, sender, receiver);
        if (!ret) throw new Error("Error in nacl box open");
        return ret;
    }

}
