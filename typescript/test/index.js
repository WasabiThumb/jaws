const WebSocket = require("isomorphic-ws");
const Jaws = require("../dist/index");

const connection = new WebSocket("ws://127.0.0.1:34345");
const client = new Jaws.Client(connection);
client.login("wasabi").then((user) => {
    console.log("Logged in as " + user.name + " (" + user.id.toString() + ")");
    console.log("Creating lobby named \"Test Lobby\"");

    client.lobbies.create("Test Lobby").then((lobby) => {
        console.log("Lobby created: " + lobby.code);

        // Log chat messages as they arrive.
        // Use lobby.chat.getMessageHistory() for a list of all canonical chat messages.
        lobby.chat.on("message", (event) => {
            console.log(`${event.message.sender.name}: ${event.content}`);
        });

        lobby.chat.broadcast("Hello everyone!"); // Send a message to everyone

        // This will show up in chat twice since the server networks whispers unconditionally.
        // The first time it shows up, it will use the original content specified.
        // The second time it shows up, it will use the content decrypted using our private key.
        lobby.chat.whisper(client.user, "Secret message to myself!");
    });
}).catch(console.error);
