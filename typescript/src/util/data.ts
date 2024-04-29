import { RawData } from "isomorphic-ws";
import * as utf8 from "@stablelib/utf8";
import * as platform from "browser-or-node";

export enum ByteOrder {
    LITTLE_ENDIAN = 0,
    BIG_ENDIAN = 1
}
export const NATIVE_ORDER: ByteOrder = (() => {
    const ab = new ArrayBuffer(2);
    const u8 = new Uint8Array(ab);
    const u16 = new Uint16Array(ab);
    u8[0] = 0xAA; u8[1] = 0xBB;
    return u16[0] === 0xBBAA ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
})();
export const NON_NATIVE_ORDER: ByteOrder = (NATIVE_ORDER === ByteOrder.BIG_ENDIAN) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;


export async function decodeSocketData(data: any): Promise<Uint8Array> {
    if (typeof data === "string") {
        return utf8.encode(data);
    } else {
        const raw: RawData = data as RawData;
        if (Array.isArray(raw)) {
            if (raw.length < 1) {
                return new Uint8Array(0);
            } else if (raw.length == 1) {
                return raw[0];
            } else {
                if (platform.isNode) {
                    return Buffer.concat(raw);
                } else {
                    const arrays: Uint8Array[] = raw as Uint8Array[];
                    let size = 0;
                    for (let array of arrays) size += array.length;
                    const dat = new Uint8Array(size);
                    let head = 0;
                    for (let array of arrays) {
                        dat.set(array, head);
                        head += array.length;
                    }
                    return dat;
                }
            }
        } else {
            if (raw instanceof ArrayBuffer) return new Uint8Array(raw);
            if (platform.isBrowser && raw instanceof Blob) {
                return (raw as Blob).arrayBuffer().then((ab) => {
                    return new Uint8Array(ab)
                });
            }
            return raw;
        }
    }
}
