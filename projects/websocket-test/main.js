const { WebSocketServer, WebSocket } = require("ws");

const PORT = 8080;
const rooms = new Map();

const wss = new WebSocketServer({ port: PORT });

function broadcast(roomId, message) {
	const clients = rooms.get(roomId);
	if (!clients) {
		return;
	}

	for (const client of clients) {
		if (client.readyState === WebSocket.OPEN) {
			client.send(message);
		}
	}
}

wss.on("connection", (ws, req) => {
	const url = new URL(req.url || "/", `ws://localhost:${PORT}`);
	const roomId = (url.searchParams.get("room") || "").trim();

	if (!roomId) {
		ws.send("Missing room query. Use: ws://localhost:8080/?room=room_id");
		ws.close();
		return;
	}

	const isNewRoom = !rooms.has(roomId);
	if (isNewRoom) {
		rooms.set(roomId, new Set());
	}

	const roomClients = rooms.get(roomId);
	roomClients.add(ws);
	ws.roomId = roomId;

	ws.send(isNewRoom ? `Room created: ${roomId}` : `Joined room: ${roomId}`);
	broadcast(roomId, `Users in room ${roomId}: ${roomClients.size}`);

	ws.on("message", (data) => {
		const message = String(data).trim();
		if (!message) {
			return;
		}

		broadcast(roomId, message);
	});

	ws.on("close", () => {
		const clients = rooms.get(roomId);
		if (!clients) {
			return;
		}

		clients.delete(ws);
		if (clients.size === 0) {
			rooms.delete(roomId);
			return;
		}

		broadcast(roomId, `Users in room ${roomId}: ${clients.size}`);
	});
});

console.log(`WebSocket server running on ws://localhost:${PORT}`);
