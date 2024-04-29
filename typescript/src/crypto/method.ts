import {EncryptHandler} from "./struct";
import NoneEncryptHandler from "./impl/none";
import RoxorEncryptHandler from "./impl/roxor";
import SodiumEncryptHandler from "./impl/nacl";

const EncryptMethod = new class {
    NONE: EncryptHandler = new NoneEncryptHandler();
    ROXOR: EncryptHandler = new RoxorEncryptHandler();
    NACL: EncryptHandler = new SodiumEncryptHandler();

    get(id: number): EncryptHandler {
        switch (id) {
            case 1:
                return this.ROXOR;
            case 2:
                return this.NACL;
            default:
                return this.NONE;
        }
    }

    values(): EncryptHandler[] {
        return [ this.NONE, this.ROXOR, this.NACL ];
    }
};
export default EncryptMethod;
