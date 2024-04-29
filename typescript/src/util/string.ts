import * as utf8 from "@stablelib/utf8";


export const StringUtil = new class {

    /**
     * Decode a possibly null-terminated array of UTF-8 bytes.
     */
    decodePossiblyNullTerm(utf: Uint8Array): string {
        for (let i=0; i < utf.length; i++) {
            if (utf[i] === 0) {
                utf = utf.subarray(0, i);
                break;
            }
        }
        return utf8.decode(utf);
    }

}

const COUNT_OPS: number[] = [
    0, 0, 1, 1, 1, 1, 1, 1,
    0, 0, 0, 0, 2, 2, 3, 0
];
export const FriendlyName = new class {

    /**
     * Count the number of codepoints in a string, or 0 if any codepoint does
     * not fall in the range U+0020 to U+FFFF, or the sequence is deemed invalid.
     * @param utf String data (UTF-8 bytes or JS string)
     */
    count(utf: Uint8Array | string): number {
        if (typeof utf === "string") utf = utf8.encode(utf);
        let skip: number = 0;
        let len: number = 0;
        let n: number;

        for (let i=0; i < utf.length; i++) {
            if (skip > 0) {
                skip--;
                continue;
            }
            n = COUNT_OPS[utf[i] >> 4];
            switch (n) {
                case 0:
                    return 0;
                case 1:
                    break;
                case 2:
                    skip = 1;
                    break;
                case 3:
                    skip = 2;
                    break;
            }
            len++;
        }

        if (skip != 0) return 0;
        return len;
    }

    /**
     * Check if 1 to 32 codepoints are present, each falling in the range U+0020 to U+FFFF.
     * @param utf String data (UTF-8 bytes or JS string)
     */
    isLegal(utf: Uint8Array | string): boolean {
        const count: number = this.count(utf);
        return count > 0 && count <= 32;
    }

};
