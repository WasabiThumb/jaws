import {User} from "./user";
import Packet from "../packet/packet";

export type FacetClass<T> = { new(): T, name: string };
export type FacetOrClass<T extends FacetContext<T>> = Facet<T> | FacetClass<Facet<T>>;

export interface FacetContext<C extends FacetContext<C>> {

    registerFacets(...facets: FacetOrClass<C>[]): void;

    getFacet<T extends Facet<C>>(clazz: FacetClass<T>): T | null;

    getFacetAssert<T extends Facet<C>>(clazz: FacetClass<T>): T;

    getAllFacets(): Facet<C>[];

}

export interface Facet<C extends FacetContext<C>> {

    onInit(ctx: C): void;

    onStart(ctx: C): void;

    onEnd(ctx: C): void;

    onConnect(ctx: C, user: User): void;

    onDisconnect(ctx: C, user: User): void;

    onMessage(ctx: C, user: User, received: Packet): void;

}
