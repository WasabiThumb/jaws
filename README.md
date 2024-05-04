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

- ✅ Communication between Java & JavaScript environments ([Browser](#for-the-browser), [NodeJS](#for-nodejs))
- 🚧 User data storage with options for persistence
- 🚧 Lobbies & matchmaking
  - ✅ Basic lobby creation & joining
  - ✅ Chat
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
Reference the [test source for Java](https://github.com/WasabiThumb/jaws/blob/master/java/src/test/java/xyz/wasabicodes/jaws/JawsTest.java) for examples of how to use the library.

#### With JitPack
Add ``com.github.WasabiThumb:jaws:master-SNAPSHOT`` from the ``jitpack.io`` maven repo as a compile dependency
to your project. See more info [here](https://jitpack.io/#WasabiThumb/jaws/master-SNAPSHOT).

#### Building Locally
1. Clone the repository and enter the ``java`` directory.
2. Do ``./gradlew clean publishToMavenLocal`` (``./gradlew.bat`` for windows)
3. Add ``xyz.wasabicodes.jaws`` as a dependency to your project
   - [Maven](https://paste.gg/p/anonymous/700fa63f1dce4a36bca6aa4e3bc1e82d/files/8e8cc77e07c34c44b5b50dcee6b6b60f/raw)
   - Gradle
     - Groovy: ``implementation 'xyz.wasabicodes:jaws:1.0.0'``
     - Kotlin DSL: ``implementation("xyz.wasabicodes:jaws:1.0.0")``

### For NodeJS
Enter your project directory and run ``npm install 'https://gitpkg.now.sh/WasabiThumb/jaws?master'``.
This is a very ugly & very temporary solution.
Reference the [test source for NodeJS](https://github.com/WasabiThumb/jaws/blob/master/typescript/test/index.js) for examples of how to use the library.

### For the Browser
Create a NodeJS project and install the Jaws client as described above. Use a tool such as
[WebPack](https://github.com/webpack/webpack) to create a bundle for the browser.
\
\
See [this summary from caniuse.com](https://caniuse.com/mdn-javascript_builtins_arraybuffer,typedarrays,promises,mdn-api_websocket,mdn-api_websocket_message_event,es5#summary)
for info on what browsers are supported. As of writing the summary suggests that ``96.48%`` of browsers
can run the Jaws client. This rises to ``98.32%`` with [polyfills for es5 and Promise](https://polyfill.io/),
the limiting factor being support for the [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket) itself.
Support could theoretically reach nearly ``100%`` with [this cursed Flash Player implementation of WebSocket](https://github.com/gimite/web-socket-js)
as well as TypedArray polyfills.
\
\
Keep in mind that "insecure websockets" (``ws://`` instead of ``wss://``) can only be used when the page protocol is
plain HTTP. Using HTTPS in modern browsers disallows use of "insecure websockets". If you elect to use Jaws
over an insecure websocket, the data will remain encrypted. You may choose to disable the Jaws native packet
encryption if you are using a secure websocket (``wss://``).
