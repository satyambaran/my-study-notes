// Single Threaded Event Loop Model
// non-blocking asynchronous

// setImmediate
// process.nextTick()

// The beauty of the event loop is not of running everything in a single thread, but it’s available to “put aside” long time-consuming I/O operations to keep the execution of other instructions.

const express = require("express");
const app = express();

let visitorCount = 0; //? In multi threaded servers, to achieve this, we will need to store that counter in some persistent storage
let counter2 = 0;

app.get("/", (req, res, next) => {
	visitorCount++;
	setTimeout((req, res) => {
		console.log("will get printed:", visitorCount, " ", counter2);
	}, 5000);
	//! since server is still running, even though we bombardment of requests, we'll return as usual. and the same number of 'will get printed' will be on console
	res.send(`Hello World, visitor counter is: ${visitorCount}, ${counter2++}`);
});
app.get("/get", async (req, res) => {
	// await fun(req.query);
	visitorCount++;
	// res.status(200).send("Status code of 200!");
	// console.log(res.status(200));
	setTimeout(async () => {
		try {
			// await fun();
			res.status(200).send(await fun());
		} catch (err) {
			res.status(400).send(err);
		}
	}, 5000);
});

let port = 8002;

app.listen(port, () => {
	console.log(`Start listening at port: ${port}`);
});
setTimeout(() => {
	port = 8080;
	app.listen(port, () => {
		console.log(`Server is running at the port: ${port}`);
	});
	setTimeout(() => {
		port = 9000;
		app.listen(port, () => {
			console.log("listening on ", port);
		});
	}, 1000);
}, 100);
// Start listening at port: 8000
// Server is running at the port: 8000
// listening on  8080

//? app is listening at all ports

async function fun() {
	return new Promise((res, rej) => {
		console.log(port);
		if (port == 9000) res("" + port);
		rej("bye");
	});
}

// ====================================================================
// Basic HTTP server without Express (from sol.js)
// ====================================================================

// const http = require("http");
// let k = http.createServer((req, res) => {
// 	let url = req.url;
// 	req.method;
// 	res.setHeader("Content-Type", "application/json");
// 	if (!url.startsWith("/project/")) {
// 		res.statusCode = 400;
// 		let ret = {
// 			status: false,
// 			message: "BAD REQUEST",
// 		};
// 		res.end(JSON.stringify(ret));
// 	} else {
// 		let stringSplit = url.split("/project/");
// 		let dataArray = [];
// 		if (dataArray.includes(stringSplit[1])) {
// 		} else {
// 		}
// 	}
// });
// k.listen(8000);

