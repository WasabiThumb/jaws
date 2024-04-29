package xyz.wasabicodes.jaws.packet.impl;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.data.PacketData;
import xyz.wasabicodes.jaws.struct.lobby.LobbyCode;

public class PacketInLobbyJoin extends PacketIn {

    public int transaction = 0;
    public int lobbyCode = 0;
    public PacketInLobbyJoin() {
        super(6);
    }

    public String getLobbyCode() {
        return LobbyCode.fromInt(this.lobbyCode);
    }

    public void setLobbyCode(String code) {
        this.lobbyCode = LobbyCode.toInt(code);
    }

    @Override
    public int getElementCount() {
        return 2;
    }

    @Override
    public void read(PacketData dat) throws IllegalArgumentException {
        int transaction = dat.readUInt8();
        int lobbyCode = dat.readInt32();
        this.transaction = transaction;
        this.lobbyCode = lobbyCode;
    }

    @Override
    public void write(PacketData dat) {
        dat.writeUInt8(this.transaction);
        dat.writeInt32(this.lobbyCode);
    }

}
