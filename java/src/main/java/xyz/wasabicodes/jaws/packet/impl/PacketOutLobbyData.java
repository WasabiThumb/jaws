package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketOut;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.util.FriendlyName;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketOutLobbyData extends PacketOut {

    public int transaction = 0;
    public int lobbyCode = 0;
    public String lobbyName = "Lobby";
    public int userCount = 0;
    public UUID[] userIDs = new UUID[0];
    public String[] userNames = new String[0];
    public int ownerIndex = 0;
    public boolean isPublic = false;
    public PacketOutLobbyData() {
        super(7);
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        final int transaction = dat.readUInt8();
        final int lobbyCode = dat.readInt32();
        final int userCount = dat.readUInt16();
        final int ownerIndex = dat.readUInt16();

        UUID[] userIDs = new UUID[userCount];
        long hi, lo;
        for (int i=0; i < userCount; i++) {
            hi = dat.readInt64();
            lo = dat.readInt64();
            userIDs[i] = new UUID(hi, lo);
        }

        final boolean isPublic = dat.readBoolean();
        int[] nameLens = new int[userCount];
        for (int i=0; i < userCount; i++) nameLens[i] = dat.readBoolean() ? 16 : 0;

        int b = 0;
        int nb;
        boolean even = true;
        for (int i=0; i < userCount; i++) {
            if (even) {
                b = dat.readUInt8();
                nb = b >> 4;
            } else {
                nb = b & 15;
            }
            nameLens[i] |= nb;
            even = !even;
        }
        String[] userNames = new String[userCount];
        for (int i=0; i < userCount; i++) {
            nb = nameLens[i] + 1;
            nb = (nb << 2) - nb; // nb *= 3
            userNames[i] = parseNullTerminated(dat.readLiteral(nb));
        }

        nb = (dat.readUInt8() & 0x1F) + 1;
        byte[] bytes = dat.readRemainingUpTo((nb << 2) - nb);
        if (!FriendlyName.isLegal(bytes)) throw new IllegalArgumentException("Illegal lobby name: " + new String(bytes, StandardCharsets.UTF_8));
        String lobbyName = new String(bytes, StandardCharsets.UTF_8);

        this.transaction = transaction;
        this.lobbyCode = lobbyCode;
        this.userCount = userCount;
        this.userIDs = userIDs;
        this.userNames = userNames;
        this.lobbyName = lobbyName;
        this.ownerIndex = ownerIndex;
        this.isPublic = isPublic;
    }

    private String parseNullTerminated(byte[] bytes) {
        for (int i=0; i < bytes.length; i++) {
            if (bytes[i] == ((byte) 0)) return new String(bytes, 0, i, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void write(PacketData dat) {
        dat.writeUInt8(this.transaction);
        dat.writeInt32(this.lobbyCode);

        dat.writeUInt16(this.userCount);
        dat.writeUInt16(this.ownerIndex);

        UUID uuid;
        int[] nameLenLo = new int[this.userCount];
        boolean[] nameLenHi = new boolean[this.userCount];
        for (int i=0; i < this.userCount; i++) {
            int nameLen = (this.userNames[i].length() - 1);
            nameLenHi[i] = (nameLen & 16) != 0;
            nameLenLo[i] = nameLen & 15;
            uuid = this.userIDs[i];
            dat.writeInt64(uuid.getMostSignificantBits());
            dat.writeInt64(uuid.getLeastSignificantBits());
        }

        dat.writeBoolean(this.isPublic);
        for (boolean b : nameLenHi) {
            dat.writeBoolean(b);
        }

        final int pairs = (this.userCount + 1) >> 1;
        int z = 0;
        for (int i=0; i < pairs; i++) {
            int b = nameLenLo[z] << 4;
            z++;
            if (z < this.userCount) {
                dat.writeUInt8(b | nameLenLo[z]);
            } else {
                dat.writeUInt8(b);
                break;
            }
            z++;
        }

        String n;
        int maxBytes;
        for (int i=0; i < this.userCount; i++) {
            n = this.userNames[i];
            maxBytes = n.length();
            maxBytes = (maxBytes << 2) - maxBytes; // maxBytes *= 3

            byte[] buf = new byte[maxBytes];
            StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(n), ByteBuffer.wrap(buf), true);
            dat.writeLiteral(buf);
        }

        dat.writeUInt8((this.lobbyName.length() - 1) & 0x1F);
        byte[] bytes = this.lobbyName.getBytes(StandardCharsets.UTF_8);
        dat.writeLiteral(bytes);
    }

}
