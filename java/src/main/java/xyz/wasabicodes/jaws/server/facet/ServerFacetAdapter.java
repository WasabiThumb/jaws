package xyz.wasabicodes.jaws.server.facet;

import xyz.wasabicodes.jaws.packet.PacketIn;
import xyz.wasabicodes.jaws.server.JawsServer;
import xyz.wasabicodes.jaws.server.struct.ServerUser;
import xyz.wasabicodes.jaws.struct.facet.Facet;

public abstract class ServerFacetAdapter implements ServerFacet{

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Facet<JawsServer>>[] getDependencies() {
        return new Class[0];
    }

    @Override
    public void onInit(JawsServer ctx) {

    }

    @Override
    public void onStart(JawsServer ctx) {

    }

    @Override
    public void onEnd(JawsServer ctx) {

    }

    @Override
    public void onConnect(JawsServer ctx, ServerUser user) {

    }

    @Override
    public void onDisconnect(JawsServer ctx, ServerUser user) {

    }

    @Override
    public void onMessage(JawsServer ctx, ServerUser user, PacketIn received) {

    }

}
