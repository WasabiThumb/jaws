import {CompressHandler} from "../struct";

export default class NoneCompressHandler implements CompressHandler {

    readonly identifier: number = 0;

    compress(bytes: Uint8Array): Uint8Array {
        return bytes;
    }

    decompress(bytes: Uint8Array): Uint8Array {
        return bytes;
    }

}
