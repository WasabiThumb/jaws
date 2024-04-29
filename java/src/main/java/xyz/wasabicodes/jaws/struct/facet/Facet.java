package xyz.wasabicodes.jaws.struct.facet;

import xyz.wasabicodes.jaws.packet.Packet;
import xyz.wasabicodes.jaws.struct.User;

public interface Facet<C extends FacetContext<C>> {

    Class<? extends Facet<C>>[] getDependencies();

    void onInit(C ctx);

    void onStart(C ctx);

    void onEnd(C ctx);

    void onConnect(C ctx, User user);

    void onDisconnect(C ctx, User user);

    void onMessage(C ctx, User user, Packet received);

}
