package xyz.wasabicodes.jaws.server.facet;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.packet.impl.PacketInPing;
import xyz.wasabicodes.jaws.packet.impl.PacketOutPong;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.struct.ServerUser;

public class PingServerFacet extends ServerFacetAdapter {

    @Override
    public void onMessage(JawsServer ctx, ServerUser user, PacketIn received) {
        if (received instanceof PacketInPing ping) {
            PacketOutPong response = new PacketOutPong();
            System.arraycopy(ping.data, 0, response.data, 0, response.data.length);
            user.sendPacket(response);
        }
    }

}
