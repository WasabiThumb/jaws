import {WebSocket} from "isomorphic-ws";
import {ByteOrder, decodeSocketData, NATIVE_ORDER} from "../util/data";
import Packet from "../packet/packet";
import PacketSerializer from "../packet/serializer";
import PacketOutPreflight from "../packet/impl/out/preflight";
import PacketInLogin from "../packet/impl/in/login";
import PacketOutSessionStart from "../packet/impl/out/sessionStart";
import {EncryptKey} from "../crypto/struct";
import {ClientLocalUser} from "./struct/user/local";
import {Facet, FacetContext, FacetClass, FacetOrClass} from "../struct/facet";
import {objectValues} from "../util/collection";
import LobbyClientFacet from "./struct/facet/lobby";

type SimpleFn = (() => void);
type AuthState = IdleAuthState | ReadyAuthState | PendingAuthState | CompleteAuthState;
type IdleAuthState = { type: "idle", callbacks: SimpleFn[] };
type ReadyAuthState = { type: "ready", preflight: PacketOutPreflight };
type PendingAuthState = { type: "pending", preflight: PacketOutPreflight, login: PacketInLogin, callbacks: SimpleFn[] };
type CompleteAuthState = { type: "complete", user: ClientLocalUser };
type PacketListener<T extends Packet> = ((packet: T) => void);
type RegisteredPacketListener = { id: Packet["id"], fun: PacketListener<Packet>, once: boolean };
type ClientFacet = Facet<JawsClient>;

export default class JawsClient implements FacetContext<JawsClient> {

    private readonly connection: WebSocket
    private readonly destructs: SimpleFn[];
    private _authState: AuthState = { type: "idle", callbacks: [] };
    private _callbacks: RegisteredPacketListener[] = [];
    private _facets: { [key: string]: ClientFacet } = {};
    constructor(connection: WebSocket) {
        this.connection = connection
        this._registerDefaultFacets();

        const me = this;
        const destructs: SimpleFn[] = [];

        const errorListener = ((e: Error) => me.onError(e));
        connection.on('error', errorListener);
        destructs.push(() => connection.off('error', errorListener));

        const messageListener = ((data: any) => {
            decodeSocketData(data).then((arr: Uint8Array) => me.onMessage(arr));
        });
        connection.on('message', messageListener);
        destructs.push(() => connection.off('message', messageListener));

        this.destructs = destructs;
    }

    private _registerDefaultFacets(): void {
        this.registerFacets(
            LobbyClientFacet
        );
    }

    get lobbies(): LobbyClientFacet {
        return this.getFacetAssert(LobbyClientFacet);
    }

    isLoggedIn(): boolean {
        return this._authState.type === "complete";
    }

    get user(): ClientLocalUser {
        if (this._authState.type !== "complete") throw new Error("Not logged in");
        return this._authState.user;
    }

    sendPacket(packet: Packet): void {
        const user: ClientLocalUser = this.user;
        let data: Uint8Array = PacketSerializer.serialize(packet, user.order);
        data = user.wrapData(data);
        this.connection.send(data);
    }

    onPacket<T extends Packet>(clazz: { new(): T }, cb: PacketListener<T>, once?: boolean): void {
        let registered: RegisteredPacketListener = {
            id: ((new clazz()).id),
            fun: cb as unknown as PacketListener<Packet>,
            once: once === true
        };
        this._callbacks.push(registered);
    }

    oncePacket<T extends Packet>(clazz: { new(): T }, cb: PacketListener<T>): void {
        this.onPacket(clazz, cb, true);
    }

    awaitPacket<T extends Packet>(clazz: { new(): T }, timeout: number = 5000): Promise<T> {
        let resolved: boolean = false;
        const me = this;
        return new Promise<T>((res, rej) => {
            let to = setTimeout(() => {
                if (resolved) return;
                rej("Request timed out");
            }, timeout);
            me.oncePacket<T>(clazz, (value: T) => {
                clearTimeout(to);
                resolved = true;
                res(value);
            });
        });
    }

    async login(username: string): Promise<ClientLocalUser> {
        if (this._authState.type === "pending" || this._authState.type === "complete") {
            throw new Error("Already logged in");
        }
        if (this._authState.type === "idle") {
            const s: IdleAuthState = this._authState;
            await new Promise<void>((res) => {
                s.callbacks.push(() => { res() });
            });
        }
        if (this._authState.type !== "ready") throw new Error("Already logged in");

        const state: ReadyAuthState = this._authState as ReadyAuthState;
        const key = state.preflight.encryption.generateLocalKey();
        const packet = new PacketInLogin();
        packet.order = state.preflight.preferredOrder;
        packet.name = username;
        packet.keyData = key;
        this.connection.send(PacketSerializer.serialize(packet, NATIVE_ORDER));

        const pendingState: PendingAuthState = { type: "pending", preflight: state.preflight, login: packet, callbacks: [] };
        this._authState = pendingState;
        await new Promise<void>((res) => {
            pendingState.callbacks.push(() => { res() });
        });
        if ((this._authState as unknown as AuthState).type !== "complete") throw new Error("Already logged in");
        return (this._authState as unknown as CompleteAuthState).user;
    }

    protected onError(e: Error) {
        console.error(e);
    }

    protected onMessage(data: Uint8Array) {
        let complete = this._authState.type === "complete";
        let order: ByteOrder = NATIVE_ORDER;
        if (complete) {
            const user = (this._authState as CompleteAuthState).user;
            order = user.order;
            try {
                data = user.unwrapData(data);
            } catch (e) {
                console.error(e);
                return;
            }
        }
        let packet: Packet;
        try {
            packet = PacketSerializer.deserialize(data, order);
        } catch (e) {
            console.error(e);
            return;
        }
        if (complete) {
            for (let cf of this.getAllFacets()) cf.onMessage(this, (this._authState as CompleteAuthState).user, packet);
            let i: number = 0;
            let cb: RegisteredPacketListener;
            while (i < this._callbacks.length) {
                cb = this._callbacks[i];
                if (cb.id !== packet.id) continue;
                try {
                    cb.fun(packet);
                } catch (e) {
                    console.error(e);
                }
                if (cb.once) {
                    this._callbacks.splice(i, 1);
                } else {
                    i++;
                }
            }
            return;
        }
        if (packet instanceof PacketOutPreflight && this._authState.type === "idle") {
            const callbacks: SimpleFn[] = this._authState.callbacks;
            this._authState = { type: "ready", preflight: packet };
            for (let cb of callbacks) cb();
            for (let cf of this.getAllFacets()) cf.onStart(this);
            return;
        }
        if (packet instanceof PacketOutSessionStart && this._authState.type === "pending") {
            const callbacks: SimpleFn[] = this._authState.callbacks;
            const user = new ClientLocalUser(
                this,
                packet.identifier,
                this._authState.login.name,
                this._authState.login.keyData as EncryptKey,
                this._authState.preflight.serverKey!,
                this._authState.login.order,
                this._authState.preflight.encryption,
                this._authState.preflight.compression
            );
            this._authState = { type: "complete", user };
            for (let cb of callbacks) cb();
            for (let cf of this.getAllFacets()) cf.onConnect(this, user);
        }
    }

    close(): void {
        if (this._authState.type === "complete") {
            for (let cf of this.getAllFacets()) cf.onDisconnect(this, this._authState.user);
        }
        for (let d of this.destructs) d();
        this.destructs.splice(0);
        for (let cf of this.getAllFacets()) cf.onEnd(this);
        this.connection.close();
    }

    getAllFacets(): ClientFacet[] {
        return objectValues(this._facets);
    }

    getFacet<T extends ClientFacet>(clazz: FacetClass<T>): T | null {
        let ret: ClientFacet | undefined = this._facets[clazz.name];
        if (!ret) return null;
        return ret as T;
    }

    getFacetAssert<T extends ClientFacet>(clazz: FacetClass<T>): T {
        const ret: T | null = this.getFacet(clazz);
        if (!ret) throw new Error("Facet is required but not present");
        return ret;
    }

    registerFacets(...facets: FacetOrClass<JawsClient>[]): void {
        const fieldTest: keyof ClientFacet = "onInit";
        for (let v of facets) {
            let facet: ClientFacet;
            let className: string;
            if (!!(v as unknown as any)[fieldTest]) {
                facet = v as ClientFacet;
                className = (facet.constructor as unknown as { name: string }).name;
            } else {
                facet = new (v as FacetClass<ClientFacet>)();
                className = (v as FacetClass<ClientFacet>).name;
            }
            try {
                facet.onInit(this);
            } finally {
                this._facets[className] = facet;
            }
        }
    }

}
