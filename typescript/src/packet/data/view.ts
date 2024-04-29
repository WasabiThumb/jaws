import {AbstractPacketData} from "../data";
import {ByteOrder, NATIVE_ORDER, NON_NATIVE_ORDER} from "../../util/data";

export default class DataViewPacketData extends AbstractPacketData {

    readonly byteOrders: ByteOrder[] = [ NATIVE_ORDER, NON_NATIVE_ORDER ];
    private readonly view: DataView;
    private order: ByteOrder = NATIVE_ORDER;
    private head: number = 0;
    private boolHead: number = 0;
    constructor(view: DataView) {
        super();
        this.view = view;
    }

    getByteOrder(): ByteOrder {
        return this.order;
    }

    setByteOrder(order: ByteOrder): void {
        this.order = order;
    }

    getSize(): number {
        return this.view.byteLength;
    }

    readBoolean(): boolean {
        const ret: boolean = (this.view.getUint8(this.head) & (128 >> this.boolHead)) != 0;
        if ((++this.boolHead) === 8) {
            this.boolHead = 0;
            this.head++;
        }
        return ret;
    }

    readInt8(): number {
        this._finishHangingBool();
        return this.view.getInt8(this.head++);
    }

    readUInt8(): number {
        this._finishHangingBool();
        return this.view.getUint8(this.head++);
    }

    readInt16(): number {
        this._finishHangingBool();
        const ret = this.view.getInt16(this.head, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 2;
        return ret;
    }

    readUInt16(): number {
        this._finishHangingBool();
        const ret = this.view.getUint16(this.head, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 2;
        return ret;
    }

    readInt32(): number {
        this._finishHangingBool();
        const ret = this.view.getInt32(this.head, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 4;
        return ret;
    }

    readLiteral(len: number): Uint8Array {
        this._finishHangingBool();
        const omni: Uint8Array = new Uint8Array(this.view.buffer, this.view.byteOffset);
        return omni.subarray(this.head, this.head += len);
    }

    readRemaining(): Uint8Array {
        this._finishHangingBool();
        const omni: Uint8Array = new Uint8Array(this.view.buffer, this.view.byteOffset);
        return omni.subarray(this.head, this.head = omni.byteLength);
    }

    readRemainingUpTo(max: number): Uint8Array {
        this._finishHangingBool();
        const omni: Uint8Array = new Uint8Array(this.view.buffer, this.view.byteOffset);
        const dest: number = Math.min(this.head + max, omni.byteLength);
        return omni.subarray(this.head, this.head = dest);
    }

    toBytes(arr?: Uint8Array, offset: number = 0): Uint8Array {
        const ret = new Uint8Array(this.view.buffer, this.view.byteOffset);
        if (typeof arr !== "undefined") {
            arr.set(ret, offset);
            return arr;
        }
        return ret;
    }

    writeBoolean(bool: boolean): void {
        if (bool) {
            let v: number = this.boolHead === 0 ? 0 : this.view.getUint8(this.head);
            v |= (128 >> this.boolHead);
            this.view.setUint8(this.head, v);
        }
        if ((++this.boolHead) === 8) {
            this.boolHead = 0;
            this.head++;
        }
    }

    writeInt8(int8: number): void {
        this._finishHangingBool();
        this.view.setInt8(this.head++, int8);
    }

    writeUInt8(uint8: number): void {
        this._finishHangingBool();
        this.view.setUint8(this.head++, uint8);
    }

    writeInt16(int16: number): void {
        this._finishHangingBool();
        this.view.setInt16(this.head, int16, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 2;
    }

    writeUInt16(uint16: number): void {
        this._finishHangingBool();
        this.view.setUint16(this.head, uint16, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 2;
    }

    writeInt32(int32: number): void {
        this._finishHangingBool();
        this.view.setInt32(this.head, int32, this.order === ByteOrder.LITTLE_ENDIAN);
        this.head += 4;
    }

    writeLiteral(literal: Uint8Array | number[]): void {
        this._finishHangingBool();
        const omni: Uint8Array = new Uint8Array(this.view.buffer, this.view.byteOffset);
        omni.set(literal, this.head);
        this.head += literal.length;
    }

    private _finishHangingBool(): void {
        if (this.boolHead !== 0) this.head++;
        this.boolHead = 0;
    }

}
