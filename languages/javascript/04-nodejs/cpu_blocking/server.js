// https://www.digitalocean.com/community/tutorials/understanding-the-event-loop-callbacks-promises-and-async-await-in-javascript

// https://www.digitalocean.com/community/tutorials/how-to-use-multithreading-in-node-js
//?		 first attempt: cpublocking, asyncblocking and nonblocking hit kr
//?		 second attempt: asyncblocking, cpublocking and then nonblocking hit kr

const express = require("express");
const app = express();
const port = process.env.PORT || 3000;
const { Worker } = require("worker_threads");
const { createWorker } = require("./four_workers");
const THREAD_COUNT = 4;
app.get("/nonblocking/", (req, res) => {
	res.status(200).send("This page is non-blocking");
});
app.get("/cpublocking", async (req, res) => {
	// no await hence it will run synchronously
	// subsequent requests will keep waiting till this is done
	let counter = 0;
	for (let i = 0; i < 20_000_000_000; i++) {
		counter++;
	}
	res.status(200).send(`result is ${counter}`);
});
app.get("/asyncblocking", async (req, res) => {
	// it will run asynchronously
	// subsequent requests will start processing
	setTimeout(() => {
		res.status(200).send(`result`);
	}, 10000);
});
function calculateCount() {
	return new Promise((resolve, reject) => {
		let counter = 0;
		for (let i = 0; i < 20_000_000_000; i++) {
			counter++;
		}
		resolve(counter);
	});
}
app.get("/blocking", async (req, res) => {
	const counter = await calculateCount(); //? promises do not provide any mechanism to make CPU-bound tasks non-blocking
	res.status(200).send(`result is ${counter}`);
});
app.get("/asyncblockingwithworker", async (req, res) => {
	//?	This creates a new thread and the code in the worker.js file starts running in the thread on another core
	const worker = new Worker("./workers.js");
	// const worker = new Worker("./four_workers.js");
	worker.on("message", (data) => {
		res.status(200).send(`result is ${data}`);
	});
	worker.on("error", (msg) => {
		res.status(404).send(`An error occurred: ${msg}`);
	});
});
app.get("/asyncblockingwithfourworker", async (req, res) => {
	//?	This creates a new thread and the code in the worker.js file starts running in the thread on another core
	const workerPromises = [];
	for (let i = 0; i < THREAD_COUNT; i++) {
		workerPromises.push(createWorker());
	}
	const thread_results = await Promise.all(workerPromises);
	const total = thread_results[0] + thread_results[1] + thread_results[2] + thread_results[3];
	res.status(200).send(`result is ${total}`);
});
app.listen(port, () => {
	console.log(`App listening on port ${port}`);
});