import {CompressHandler} from "../struct";
import { inflate, deflate } from "pako";

export default class ZLibCompressHandler implements CompressHandler {

    readonly identifier: number = 1;

    compress(bytes: Uint8Array): Uint8Array {
        return deflate(bytes);
    }

    decompress(bytes: Uint8Array): Uint8Array {
        return inflate(bytes);
    }

}
