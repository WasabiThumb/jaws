
export interface CompressHandler {

    readonly identifier: number;

    compress(bytes: Uint8Array): Uint8Array;

    decompress(bytes: Uint8Array): Uint8Array;

}
