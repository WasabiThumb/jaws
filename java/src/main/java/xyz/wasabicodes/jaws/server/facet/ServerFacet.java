package xyz.wasabicodes.jaws.server.facet;

import xyz.wasabicodes.jaws.packet.Packet;
import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.struct.ServerUser;
import xyz.wasabicodes.jaws.struct.User;
import xyz.wasabicodes.jaws.struct.facet.Facet;

public interface ServerFacet extends Facet<JawsServer> {

    void onConnect(JawsServer ctx, ServerUser user);

    @Override
    default void onConnect(JawsServer ctx, User user) {
        this.onConnect(ctx, (ServerUser) user);
    }

    void onDisconnect(JawsServer ctx, ServerUser user);

    @Override
    default void onDisconnect(JawsServer ctx, User user) {
        this.onDisconnect(ctx, (ServerUser) user);
    }

    void onMessage(JawsServer ctx, ServerUser user, PacketIn received);

    @Override
    default void onMessage(JawsServer ctx, User user, Packet received) {
        this.onMessage(ctx, (ServerUser) user, (PacketIn) received);
    }

}
