package xyz.wasabicodes.jaws.crypto.nacl;

import xyz.wasabicodes.jaws.crypto.EncryptKey;

interface SodiumEncryptKey extends EncryptKey {

    byte[] getPublic();

}
