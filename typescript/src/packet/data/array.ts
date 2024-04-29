import {AbstractPacketData} from "../data";
import {ByteOrder, NATIVE_ORDER, NON_NATIVE_ORDER} from "../../util/data";

function flip(buf: Uint8Array, offset: number, span: number) {
    if (span < 1) return;
    let a: number = offset;
    let b: number = offset + (span << 1) - 1;
    let carry: number;
    for (let z=0; z < span; z++) {
        carry = buf[a];
        buf[a++] = buf[b];
        buf[b--] = carry;
    }
}

export default class TypedArrayPacketData extends AbstractPacketData {

    readonly byteOrders: ByteOrder[] = [ NATIVE_ORDER, NON_NATIVE_ORDER ];

    private head: number = 0;
    private boolHead: number = 0;
    private order: ByteOrder = NATIVE_ORDER;
    private readonly u8: Uint8Array;
    private readonly i8: Int8Array;
    private readonly max8: number;
    private readonly u16: Uint16Array;
    private readonly i16: Int16Array;
    private readonly max16: number;
    private readonly i32: Int32Array;
    private readonly max32: number;

    constructor(dat: Uint8Array) {
        super();

        const emptyBuf: ArrayBuffer = new ArrayBuffer(0);
        const buf: ArrayBuffer = dat.buffer;
        const size: number = dat.byteLength;
        const offset: number = dat.byteOffset;
        this.u8 = dat;
        this.i8 = new Int8Array(buf, offset, size);
        this.max8 = size;

        if ((size & 1) == 0) {
            this.u16 = new Uint16Array(buf, offset, size);
            this.i16 = new Int16Array(buf, offset, size);
            this.max16 = size >> 1;
        } else {
            this.u16 = new Uint16Array(emptyBuf);
            this.i16 = new Int16Array(emptyBuf);
            this.max16 = 0;
        }

        if ((size & 3) == 0) {
            this.i32 = new Int32Array(buf, offset, size);
            this.max32 = size >> 2;
        } else {
            this.i32 = new Int32Array(emptyBuf);
            this.max32 = 0;
        }
    }

    getByteOrder(): ByteOrder {
        return this.order;
    }

    setByteOrder(order: ByteOrder): void {
        this.order = order;
    }

    getSize(): number {
        return this.u8.length;
    }

    readUInt8(): number {
        this._finishHangingBool();
        return this.u8[this.head++];
    }

    readInt8(): number {
        this._finishHangingBool();
        return this.i8[this.head++];
    }

    readUInt16(): number {
        return this._readInt(1, 1, 2, this.max16, this.u16, (buf) => new Uint16Array(buf));
    }

    readInt16(): number {
        return this._readInt(1, 1, 2, this.max16, this.i16, (buf) => new Int16Array(buf));
    }

    readInt32(): number {
        return this._readInt(3, 2, 4, this.max32, this.i32, (buf) => new Int32Array(buf));
    }

    private _readInt<T extends Uint16Array | Int16Array | Int32Array>(flag: number, span: number, bytes: number, max: number, arr: T, newArray: (buf: ArrayBuffer) => T): T[number] {
        this._finishHangingBool();
        const nonNative: boolean = this.order === NON_NATIVE_ORDER;
        if ((this.head & flag) === 0) {
            let pos: number = this.head >> span;
            if (pos < max) {
                if (nonNative) flip(this.u8, this.head, span);
                this.head += bytes;
                return arr[pos];
            }
        }
        const u8: Uint8Array = this.readLiteral(bytes);
        if (nonNative) flip(u8, 0, span);
        return newArray(u8.buffer)[0];
    }

    readBoolean(): boolean {
        const ret: boolean = (this.u8[this.head] & (128 >> this.boolHead)) != 0;
        if ((++this.boolHead) === 8) {
            this.boolHead = 0;
            this.head++;
        }
        return ret;
    }

    readLiteral(len: number): Uint8Array {
        this._finishHangingBool();
        return this.u8.subarray(this.head, this.head += len);
    }

    readRemaining(): Uint8Array {
        this._finishHangingBool();
        return this.u8.subarray(this.head, this.head = this.max8);
    }

    readRemainingUpTo(max: number): Uint8Array {
        this._finishHangingBool();
        return this.u8.subarray(this.head, this.head = Math.min(this.head + max, this.max8));
    }

    toBytes(arr?: Uint8Array, offset: number = 0): Uint8Array {
        if (typeof arr !== "undefined") {
            arr.set(this.u8, offset);
            return arr;
        } else {
            return this.u8;
        }
    }

    writeUInt8(uint8: number): void {
        this._finishHangingBool();
        this.u8[this.head++] = uint8;
    }

    writeInt8(int8: number): void {
        this._finishHangingBool();
        this.i8[this.head++] = int8;
    }

    writeUInt16(uint16: number): void {
        this._writeInt(1, 1, 2, this.max16, this.u16, (buf) => new Uint16Array(buf), uint16);
    }

    writeInt16(int16: number): void {
        this._writeInt(1, 1, 2, this.max16, this.i16, (buf) => new Int16Array(buf), int16);
    }

    writeInt32(int32: number): void {
        this._writeInt(3, 2, 4, this.max32, this.i32, (buf) => new Int32Array(buf), int32);
    }

    private _writeInt<T extends Uint16Array | Int16Array | Int32Array>(flag: number, span: number, bytes: number, max: number, arr: T, newArray: (buf: ArrayBuffer) => T, n: T[number]): void {
        this._finishHangingBool();
        const nonNative: boolean = this.order === NON_NATIVE_ORDER;
        let subPos: number;
        if ((this.head & flag) === 0 && (subPos = (this.head >> span)) < max) {
            arr[subPos] = n;
            if (nonNative) flip(this.u8, this.head, span);
            this.head += bytes;
        } else {
            const buf: ArrayBuffer = new ArrayBuffer(bytes);
            const u8: Uint8Array = new Uint8Array(buf);
            newArray(buf)[0] = n;
            if (nonNative) flip(u8, 0, span);
            this.writeLiteral(u8);
        }
    }

    writeBoolean(bool: boolean): void {
        if (bool) {
            let v: number = this.boolHead === 0 ? 0 : this.u8[this.head];
            v |= (128 >> this.boolHead);
            this.u8[this.head] = v;
        }
        if ((++this.boolHead) === 8) {
            this.boolHead = 0;
            this.head++;
        }
    }

    writeLiteral(literal: Uint8Array | number[]): void {
        this._finishHangingBool();
        this.u8.set(literal, this.head);
        this.head += literal.length;
    }

    private _finishHangingBool(): void {
        if (this.boolHead !== 0) this.head++;
        this.boolHead = 0;
    }

}
