const WebSocket = require("isomorphic-ws");
const Jaws = require("../dist/index");

const connection = new WebSocket("ws://127.0.0.1:34345");
const client = new Jaws.Client(connection);
client.login("wasabi").then((user) => {
    console.log("Logged in as " + user.name + " (" + user.id.toString() + ")");
    console.log("Creating lobby named \"Test Lobby\"");

    client.lobbies.create("Test Lobby").then((lobby) => {
        console.log("Lobby created: " + lobby.code);
    });
}).catch(console.error);
