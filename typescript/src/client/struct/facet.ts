import {Facet} from "../../struct/facet";
import JawsClient from "../client";
import {User} from "../../struct/user";
import Packet from "../../packet/packet";

export interface ClientFacet extends Facet<JawsClient> {
}

export abstract class ClientFacetAdapter implements ClientFacet {

    onConnect(ctx: JawsClient, user: User): void {
    }

    onDisconnect(ctx: JawsClient, user: User): void {
    }

    onEnd(ctx: JawsClient): void {
    }

    onInit(ctx: JawsClient): void {
    }

    onMessage(ctx: JawsClient, user: User, received: Packet): void {
    }

    onStart(ctx: JawsClient): void {
    }

}
