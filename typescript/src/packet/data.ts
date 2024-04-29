import {ByteOrder, NATIVE_ORDER, NON_NATIVE_ORDER} from "../util/data";
import ieee754 from "ieee754";
import Long from "long";
import * as utf8 from "@stablelib/utf8";

export interface PacketData {

    readonly byteOrders: ByteOrder[];

    getSize(): number;

    getByteOrder(): ByteOrder;

    setByteOrder(order: ByteOrder): void;

    toBytes(arr?: Uint8Array, offset?: number): Uint8Array;

    //

    readInt8(): number;

    readUInt8(): number;

    readInt16(): number;

    readUInt16(): number;

    readInt32(): number;

    readUInt32(): Long;

    readInt64(): Long;

    readFloat32(): number;

    readFloat64(): number;

    readBoolean(): boolean;

    readChar(): string;

    readUTF8(): string;

    readLiteral(len: number): Uint8Array;

    readRemaining(): Uint8Array;

    readRemainingUpTo(max: number): Uint8Array;

    //

    writeInt8(int8: number): void;

    writeUInt8(uint8: number): void;

    writeInt16(int16: number): void;

    writeUInt16(uint16: number): void;

    writeInt32(int32: number): void;

    writeUInt32(uint32: Long): void;

    writeInt64(int64: Long): void;

    writeFloat32(float32: number): void;

    writeFloat64(float64: number): void;

    writeBoolean(bool: boolean): void;

    writeChar(char: string): void;

    writeUTF8(utf8: string): void;

    writeLiteral(literal: Uint8Array | number[]): void;

}

export abstract class AbstractPacketData implements PacketData {

    abstract readonly byteOrders: ByteOrder[];

    abstract getByteOrder(): ByteOrder;

    abstract getSize(): number;

    abstract readBoolean(): boolean;

    readChar(): string {
        return String.fromCharCode(this.readUInt16());
    }

    readFloat32(): number {
        const bytes: Uint8Array = this.readLiteral(4);
        return ieee754.read(bytes, 0, this.getByteOrder() === ByteOrder.LITTLE_ENDIAN, 23, 4);
    }

    readFloat64(): number {
        const bytes: Uint8Array = this.readLiteral(8);
        return ieee754.read(bytes, 0, this.getByteOrder() === ByteOrder.LITTLE_ENDIAN, 52, 8);
    }

    abstract readInt16(): number;

    abstract readInt32(): number;

    readInt64(): Long {
        return Long.fromBytes(this.readLiteral(8) as unknown as number[], false, this.getByteOrder() === ByteOrder.LITTLE_ENDIAN);
    }

    abstract readInt8(): number;

    abstract readLiteral(len: number): Uint8Array;

    abstract readRemaining(): Uint8Array;

    abstract readRemainingUpTo(max: number): Uint8Array;

    abstract readUInt16(): number;

    readUInt32(): Long {
        return Long.fromInt(this.readInt32(), true);
    }

    abstract readUInt8(): number;

    readUTF8(): string {
        let len: number = this.readUInt8();
        if (len === 255) len = this.readInt32();
        let bytes: Uint8Array = this.readLiteral(len);
        return utf8.decode(bytes);
    }

    abstract setByteOrder(order: ByteOrder): void;

    abstract toBytes(arr?: Uint8Array, offset?: number): Uint8Array;

    abstract writeBoolean(bool: boolean): void;

    writeChar(char: string): void {
        if (char.length < 1) throw new Error("Character is empty");
        this.writeUInt16(char.charCodeAt(0));
    }

    writeFloat32(float32: number): void {
        const buf: Uint8Array = new Uint8Array(4);
        ieee754.write(buf, float32, 0, this.getByteOrder() === ByteOrder.LITTLE_ENDIAN, 23, 4);
        this.writeLiteral(buf);
    }

    writeFloat64(float64: number): void {
        const buf: Uint8Array = new Uint8Array(8);
        ieee754.write(buf, float64, 0, this.getByteOrder() === ByteOrder.LITTLE_ENDIAN, 52, 8);
        this.writeLiteral(buf);
    }

    abstract writeInt16(int16: number): void;

    abstract writeInt32(int32: number): void;

    writeInt64(int64: Long): void {
        this.writeLiteral(int64.toBytes(this.getByteOrder() === ByteOrder.LITTLE_ENDIAN));
    }

    abstract writeInt8(int8: number): void;

    abstract writeLiteral(literal: Uint8Array | number[]): void;

    abstract writeUInt16(uint16: number): void;

    writeUInt32(uint32: Long): void {
        this.writeInt32(uint32.toSigned().toInt());
    }

    abstract writeUInt8(uint8: number): void;

    writeUTF8(utf: string): void {
        const bytes: Uint8Array = utf8.encode(utf);
        const len = bytes.length;
        if (len >= 255) {
            this.writeUInt8(255);
            this.writeInt32(len);
        } else {
            this.writeUInt8(len & 0xFF);
        }
        this.writeLiteral(bytes);
    }

}

export abstract class WriteOnlyPacketData extends AbstractPacketData {

    static readonly ERR_TEXT: string = "Cannot read from write-only packet data";

    readBoolean(): boolean {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readInt16(): number {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readInt32(): number {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readInt8(): number {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readLiteral(len: number): Uint8Array {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readRemaining(): Uint8Array {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readRemainingUpTo(max: number): Uint8Array {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readUInt16(): number {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

    readUInt8(): number {
        throw new Error(WriteOnlyPacketData.ERR_TEXT);
    }

}

class DynamicPacketDataEntry<T> {

    readonly magic: number = 0;
    value: T;
    size: number;
    writer: ((dat: PacketData, value: T) => void);
    constructor(value: T, size: number, writer: ((dat: PacketData, value: T) => void)) {
        this.value = value;
        this.size = size;
        this.writer = writer;
    }

    write(dat: PacketData): void {
        this.writer(dat, this.value);
    }

}

const OCTET_MAGIC: number = 0x0C7E7;
class OctetDynamicPacketDataEntry extends DynamicPacketDataEntry<number> {

    readonly magic: number = OCTET_MAGIC;
    octetLength: number = 1;
    constructor(value: boolean) {
        super(value ? 128 : 0, 1, (dat: PacketData, value: number) => dat.writeUInt8(value));
    }

    put(n: boolean): void {
        if (n) this.value |= (128 >> this.octetLength);
        this.octetLength++;
    }

}

type MakeSizedFn = (buffer: Uint8Array) => PacketData;
const hasArrayConstructor: boolean = (typeof Array === "function");
export class DynamicPacketData extends WriteOnlyPacketData {

    readonly byteOrders: ByteOrder[] = [ NATIVE_ORDER, NON_NATIVE_ORDER ];
    private order: ByteOrder = NATIVE_ORDER;
    private readonly _entries: DynamicPacketDataEntry<any>[];
    private _entryHead: number = 0;
    private readonly _makeSized: MakeSizedFn;

    constructor(makeSized: MakeSizedFn, initialCapacity: number = 0) {
        super();
        this._entries = (hasArrayConstructor ? new Array(Math.max(initialCapacity, 0)) : []);
        this._makeSized = makeSized;
    }

    getByteOrder(): ByteOrder {
        return this.order;
    }

    setByteOrder(order: ByteOrder): void {
        this.order = order;
    }

    getSize(): number {
        let ret: number = 0;
        for (let i=0; i < this._entryHead; i++) ret += this._entries[i].size;
        return ret;
    }

    toBytes(arr?: Uint8Array, offset: number = 0): Uint8Array {
        const ret: Uint8Array = typeof arr !== "undefined" ? arr : new Uint8Array(this.getSize());
        const dat: PacketData = this._makeSized(ret);
        dat.setByteOrder(this.order);
        if (offset > 0) dat.readLiteral(offset);
        for (let i=0; i < this._entryHead; i++) this._entries[i].write(dat);
        return ret;
    }

    private _pushEntry0(entry: DynamicPacketDataEntry<any>) {
        if (this._entryHead >= this._entries.length) {
            this._entryHead = this._entries.push(entry);
        } else {
            this._entries[this._entryHead++] = entry;
        }
    }

    private _pushEntry<T>(value: T, size: number, key: keyof PacketData) {
        const entry = new DynamicPacketDataEntry<T>(value, size, (dat: PacketData, v: T) => {
            (dat[key] as unknown as (iv: T) => void)(v);
        });
        this._pushEntry0(entry);
    }

    writeInt8(int8: number): void {
        this._pushEntry(int8, 1, "writeInt8");
    }

    writeUInt8(uint8: number): void {
        this._pushEntry(uint8, 1, "writeUInt8");
    }

    writeInt16(int16: number): void {
        this._pushEntry(int16, 2, "writeInt16");
    }

    writeUInt16(uint16: number): void {
        this._pushEntry(uint16, 2, "writeUInt16");
    }

    writeInt32(int32: number): void {
        this._pushEntry(int32, 4, "writeInt32");
    }

    writeUInt32(uint32: Long): void {
        this._pushEntry(uint32, 4, "writeUInt32");
    }

    writeInt64(int64: Long): void {
        this._pushEntry(int64, 8, "writeInt64");
    }

    writeFloat32(float32: number): void {
        this._pushEntry(float32, 4, "writeFloat32");
    }

    writeFloat64(float64: number): void {
        this._pushEntry(float64, 8, "writeFloat64");
    }

    writeBoolean(bool: boolean): void {
        let last: DynamicPacketDataEntry<any>;
        if (this._entryHead > 0 && (last = this._entries[this._entryHead - 1]).magic === OCTET_MAGIC) {
            const octet: OctetDynamicPacketDataEntry = last as OctetDynamicPacketDataEntry;
            if (octet.octetLength < 8) {
                octet.put(bool);
                return;
            }
        }
        this._pushEntry0(new OctetDynamicPacketDataEntry(bool));
    }

    writeChar(char: string): void {
        this._pushEntry(char, 2, "writeChar");
    }

    writeLiteral(literal: Uint8Array | number[]): void {
        this._pushEntry(literal, literal.length, "writeLiteral");
    }

}

