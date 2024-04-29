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


## Using
Don't. You shouldn't use this yet. However, if you would like to anyways (and I'm mostly writing
this here for myself), here's a guide.

### For Java (17)
1. Clone the repository and enter the ``java`` directory.
2. Do ``./gradlew clean publishToMavenLocal`` (``./gradlew.bat`` for windows)
3. Add ``xyz.wasabicodes.jaws`` as a dependency to your project
  - [Maven](https://paste.gg/p/anonymous/700fa63f1dce4a36bca6aa4e3bc1e82d/files/8e8cc77e07c34c44b5b50dcee6b6b60f/raw)
  - Gradle
    - Groovy: ``implementation 'xyz.wasabicodes:jaws:1.0.0'``
    - Kotlin DSL: ``implementation("xyz.wasabicodes:jaws:1.0.0")``

### For NodeJS
Enter your project directory and run ``npm install 'https://gitpkg.now.sh/WasabiThumb/jaws/typescript?master'``.
This is a very ugly & very temporary solution.
