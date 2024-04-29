import Long from "long";
import nacl from "tweetnacl";
import * as utf8 from "@stablelib/utf8";
import { md5 } from 'js-md5';


const REGEX: RegExp = /^([\da-f]{8})(-?)([\da-f]{4})\2([\da-f]{4})\2([\da-f]{4})\2([\da-f]{12})$/i
const HEX_DIGITS: string = "0123456789abcdef";
const octetHex = ((octet: number) => {
    return HEX_DIGITS[octet >> 4] + HEX_DIGITS[octet & 0xF];
});

const octetsHex = ((octets: number[], off: number, len: number) => {
    let ret = "";
    for (let i=0; i < len; i++) ret += octetHex(octets[off + i]);
    return ret;
});

const hexNibble = ((hex: string, off: number) => {
    const code: number = hex.charCodeAt(off);
    if (code < 58) return code - 48;
    if (code < 91) return code - 55;
    return code - 87;
});

const hexOctet = ((hex: string, off: number) => {
    let z: number = off << 1;
    return (hexNibble(hex, z) << 4) | hexNibble(hex, z | 1);
});

export default class UUID {

    static fromString(str: string): UUID {
        const match = REGEX.exec(str);
        if (!match) throw new Error("String \"" + str + "\" is not a valid UUIDv4");
        const hex: string = match[1] + match[3] + match[4] + match[5] + match[6];
        const hi: number[] = new Array(8);
        const lo: number[] = new Array(8);
        let z: number = 0;
        while (z < 8) hi[z] = hexOctet(hex, z++);
        while (z < 16) lo[z & 7] = hexOctet(hex, z++);
        return new UUID(Long.fromBytesBE(hi), Long.fromBytesBE(lo));
    }

    static randomUUID(): UUID {
        const bytes: Uint8Array = nacl.randomBytes(16);
        // Set version to 4 (Random) and variant to 2 (RFC 4122)
        bytes[6] = ( bytes[6] & 15 ) | 64;
        bytes[8] = ( bytes[8] & 63 ) | 128;
        return new UUID(
            Long.fromBytesBE(bytes.subarray(0, 8) as unknown as number[]),
            Long.fromBytesBE(bytes.subarray(8, 16) as unknown as number[])
        );
    }

    static nameUUID(name: Uint8Array | number[] | string) {
        if (typeof name === "string") name = utf8.encode(name);
        return this.nameUUIDFromBytes(name);
    }

    static nameUUIDFromBytes(name: Uint8Array | number[]) {
        name = new Uint8Array(md5.arrayBuffer(name));
        // Set version to 3 (Name) and variant to 2 (RFC 4122)
        name[6] = ( name[6] & 15 ) | 48;
        name[8] = ( name[8] & 63 ) | 128;
        return new UUID(
            Long.fromBytesBE(name.subarray(0, 8) as unknown as number[]),
            Long.fromBytesBE(name.subarray(8, 16) as unknown as number[])
        );
    }

    mostSignificantBits: Long;
    leastSignificantBits: Long;
    constructor(mostSignificantBits: Long, leastSignificantBits: Long) {
        this.mostSignificantBits = mostSignificantBits;
        this.leastSignificantBits = leastSignificantBits;
    }

    get version(): number {
        return this.mostSignificantBits
            .shr(12)
            .and(15)
            .toInt();
    }

    get variant(): number {
        return this.leastSignificantBits
            .shru(Long.fromInt(64).sub(this.leastSignificantBits.shru(62)).toInt())
            .and(this.leastSignificantBits.shr(63))
            .toInt();
    }

    public toString = (): string => {
        const hi: number[] = this.mostSignificantBits.toBytesBE();
        const lo: number[] = this.leastSignificantBits.toBytesBE();

        return octetsHex(hi, 0, 4) + "-" +
            octetsHex(hi, 4, 2) + "-" +
            octetsHex(hi, 6, 2) + "-" +
            octetsHex(lo, 0, 2) + "-" +
            octetsHex(lo, 2, 6)
    }

    compare(other: UUID): number {
        let cmp: number = this.mostSignificantBits.compare(other.mostSignificantBits);
        if (cmp === 0) cmp = this.leastSignificantBits.compare(other.leastSignificantBits);
        return cmp;
    }

}
