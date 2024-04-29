/**
 *
 | Code | Class                           | Function                                                                                                             |
 |-----:|---------------------------------|----------------------------------------------------------------------------------------------------------------------|
 |    0 | PacketOutPreflight              | Sent immediately by the server to inform what encryption, compression & endianness to use.                           |
 |    1 | PacketInLogin                   | Sent by the client to log in with the given username. Key exchange may also happen here.                             |
 |    2 | PacketOutSessionStart           | Sent in response to PacketInLogin to signify a successful login, delivering the newly generated user ID.             |
 |    3 | PacketInPing                    | Ping the server with a payload of 512 bytes.                                                                         |
 |    4 | PacketOutPong                   | Respond to PacketInPing with the same payload that it was given.                                                     |
 |    5 | PacketInLobbyCreate             | Create a lobby.                                                                                                      |
 |    6 | PacketInLobbyJoin               | Join a lobby.                                                                                                        |
 |    7 | PacketOutLobbyData              | Delivers the full state of the receiver's lobby, may be sent in response to PacketInLobbyCreate & PacketInLobbyJoin. |
 |    8 | PacketInLobbyRequestPeerKey     | Request the public key of a user within the same lobby. Used for private messages.                                   |
 |    9 | PacketOutLobbyPeerKey           | Respond to PacketInLobbyRequestPeerKey with the public key of the peer.                                              |
 |   10 | PacketInLobbyChat               | Creates a user chat message: basic or whisper.                                                                       |
 |   11 | PacketOutLobbyChat              | Propagates a chat message to its targets. May also be used to broadcast a system message from the server.            |
 |   12 | PacketInLobbyRequestChatHistory | Request the chat history of the current lobby.                                                                       |
 |   13 | PacketOutLobbyChatHistory       | Respond to PacketInLobbyRequestChatHistory.                                                                          |
 */
package xyz.wasabicodes.jaws.packet.impl;