# JAWS
**JAva WebSocket**. Not an original name. Not an original concept. However, it is all mine ;)

As the name suggests, this is a client & server impl meant for use in web applications. The
focus is on web games, however it is designed to be fairly modular and can be adapted to
multiple use cases.

⚠️ **THIS IS NOT EVEN REMOTELY DONE! I'm uploading this project because I believe in FOSS, but I will
not be supporting this in any way! There will be no documentation until it is done. Refer to the
key below to know what has been implemented.** ⚠️

| Key | |
| :-: | :-- |
| ✅ | Implemented |
| 🐛 | Implemented with bugs |
| 🚧 | Work in progress |
| ❌ | Will not be implemented |

## Features

- ✅ Communication between Java & JavaScript environments (Browser, NodeJS)
- 🚧 User data storage with options for persistence
- 🚧 Lobbies & matchmaking
  - ✅ Basic lobby creation & joining
  - 🚧 Chat
  - 🚧 Matchmaking (with regional load balancing)
- 🐛 Packet compression
  - ZLib is functional on the client. Server is not a fan of the
    header for some reason. As it stands, ZLib only grows most of the
    implemented packets, so this fine. Default is no encryption.
- ✅ Packet encryption
  - ✅ Strong asymmetric encryption with NaCL
  - ✅ Light & fast obfuscation to fight low-skill scraping
- ✅ Negotiates endianness
  - Currently just blindly accepts the server's preferred endianness. May look into creating
    a browser benchmark to evaluate what is best for both parties - the server supports this.

<br>

| | Java | TypeScript (Browser) | TypeScript (Node) |
| :-: | :-: | :-: | :-: |
| Client | ❌ |  ✔️  | ✔️ |
| Server | ✔️ | ❌  | 🚧 |
