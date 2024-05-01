import Packet from "./packet";
import {ByteOrder} from "../util/data";
import {PacketData} from "./data";
import PacketOutPreflight from "./impl/out/preflight";
import PacketInLogin from "./impl/in/login";
import PacketOutSessionStart from "./impl/out/sessionStart";
import PacketInPing from "./impl/in/ping";
import PacketOutPong from "./impl/out/pong";
import PacketInLobbyCreate from "./impl/in/lobbyCreate";
import PacketInLobbyJoin from "./impl/in/lobbyJoin";
import PacketOutLobbyData from "./impl/out/lobbyData";
import PacketInLobbyChat from "./impl/in/lobbyChat";
import PacketInLobbyRequestChatHistory from "./impl/in/lobbyRequestChatHistory";
import PacketOutLobbyChat from "./impl/out/lobbyChat";
import PacketOutLobbyChatHistory from "./impl/out/lobbyChatHistory";
import PacketInLobbyRequestPeerKey from "./impl/in/lobbyRequestPeerKey";
import PacketOutLobbyPeerKey from "./impl/out/lobbyPeerKey";

type PacketClass = { new(): Packet };
export default abstract class PacketSerializer {

    private static readonly REGISTRY: { [id: number]: PacketClass } = {};
    private static INIT_REGISTRY: boolean = false;

    private static registerDefaults() {
        this.register(
            PacketOutPreflight,
            PacketInLogin,
            PacketOutSessionStart,
            PacketInPing,
            PacketOutPong,
            PacketInLobbyCreate,
            PacketInLobbyJoin,
            PacketOutLobbyData,
            PacketInLobbyRequestPeerKey,
            PacketOutLobbyPeerKey,
            PacketInLobbyChat,
            PacketInLobbyRequestChatHistory,
            PacketOutLobbyChat,
            PacketOutLobbyChatHistory,
        );
        this.INIT_REGISTRY = true;
    }

    private static register(...classes: PacketClass[]) {
        for (let cls of classes) this.REGISTRY[(new cls()).id] = cls;
    }

    static serialize(packet: Packet, order: ByteOrder): Uint8Array {
        const dat: PacketData = Packet.createData(packet.getElementCount());
        dat.setByteOrder(order);
        packet.write(dat);

        const ret: Uint8Array = new Uint8Array(dat.getSize() + 1);
        ret[0] = packet.id;
        dat.toBytes(ret, 1);
        return ret;
    }

    static deserialize(payload: Uint8Array, order: ByteOrder): Packet {
        const size: number = payload.length;
        if (size < 1) throw new Error("Payload is empty");

        const dat: PacketData = Packet.wrapData(payload);
        dat.setByteOrder(order);
        const id: number = dat.readUInt8();

        if (!this.INIT_REGISTRY) this.registerDefaults();
        let cls: PacketClass | undefined = this.REGISTRY[id];
        if (!cls) throw new Error("Unknown packet ID " + payload[0]);
        const p: Packet = new cls();
        p.read(dat);
        return p;
    }

}
