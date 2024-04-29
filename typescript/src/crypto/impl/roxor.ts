import {EncryptHandler, EncryptKey} from "../struct";
import nacl from "tweetnacl";

function ror(bits: number, shift: number): number {
    if (shift === 0) return bits;
    return (bits >>> shift) | ((bits << (8 - shift)) & 0xFF);
}

function rol(bits: number, shift: number): number {
    if (shift === 0) return bits;
    return ((bits << shift) & 0xFF) | (bits >>> (8 - shift));
}

class RoxorEncryptKey implements EncryptKey {

    readonly data: Uint8Array;
    constructor(data: Uint8Array) {
        this.data = data;
    }

    export(): Uint8Array {
        return this.data;
    }

    encrypt(b: number): number {
        let shift: number;
        let mask: number;
        let token: number;
        for (let i=0; i < this.data.length; i++) {
            token = this.data[i];
            shift = token >>> 5;
            mask = token & 0x1F;
            b = ror(b, shift) ^ mask;
        }
        return b;
    }

    decrypt(b: number): number {
        let shift: number;
        let mask: number;
        let token: number;
        for (let i=(this.data.length - 1); i >= 0; i--) {
            token = this.data[i];
            shift = token >>> 5;
            mask = token & 0x1F;
            b = rol(b ^ mask, shift);
        }
        return b;
    }

}

export default class RoxorEncryptHandler implements EncryptHandler {

    readonly identifier: number = 1;
    readonly keySize: number = 16;

    generateLocalKey(): EncryptKey {
        return new RoxorEncryptKey(nacl.randomBytes(this.keySize));
    }

    importRemoteKey(data: Uint8Array): EncryptKey {
        if (data.length < this.keySize) throw new Error("Data is not " + this.keySize + " bytes long");
        return new RoxorEncryptKey(data);
    }

    encrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        return RoxorEncryptHandler._keyOperation(data, senderKey as RoxorEncryptKey, "encrypt");
    }

    decrypt(data: Uint8Array, senderKey: EncryptKey, receiverKey: EncryptKey): Uint8Array {
        return RoxorEncryptHandler._keyOperation(data, senderKey as RoxorEncryptKey, "decrypt");
    }

    private static _keyOperation(data: Uint8Array, key: RoxorEncryptKey, fn: "encrypt" | "decrypt"): Uint8Array {
        const size = data.length;
        const ret = new Uint8Array(size);
        for (let i=0; i < size; i++) ret[i] = (key[fn])(data[i]);
        return ret;
    }

}
