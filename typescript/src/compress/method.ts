import {CompressHandler} from "./struct";
import NoneCompressHandler from "./impl/none";
import ZLibCompressHandler from "./impl/zlib";

const CompressMethod = new class {
    NONE: CompressHandler = new NoneCompressHandler();
    ZLIB: CompressHandler = new ZLibCompressHandler();

    get(id: number): CompressHandler {
        return id === 1 ? this.ZLIB : this.NONE;
    }

    values(): CompressHandler[] {
        return [ this.NONE, this.ZLIB ];
    }
};
export default CompressMethod;
