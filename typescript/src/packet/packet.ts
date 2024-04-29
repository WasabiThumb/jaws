import {PacketData, DynamicPacketData} from "./data";
import DataViewPacketData from "./data/view";
import TypedArrayPacketData from "./data/array";


const supportsDataView: boolean = (typeof DataView === "function");
export default abstract class Packet {

    static wrapData(buf: Uint8Array, useDataViewIfAvailable: boolean = true): PacketData {
        if (supportsDataView && useDataViewIfAvailable) {
            return new DataViewPacketData(new DataView(buf.buffer, buf.byteOffset, buf.byteLength));
        }
        return new TypedArrayPacketData(buf);
    }

    static createData(elementCapacity: number = 0): PacketData {
        return new DynamicPacketData(Packet.wrapData, elementCapacity);
    }

    //

    readonly id: number;
    protected constructor(id: number) {
        this.id = id;
    }

    abstract getElementCount(): number;

    abstract read(dat: PacketData): void;

    abstract write(dat: PacketData): void;

    transmitPlain(): boolean {
        return false;
    }

}
