// Process contains all the details about nodejs runtime

// Display process ID
console.log(`Process ID: ${process.pid}`);

// Display Node.js version
console.log(`Node.js version: ${process.version}`);

// Display environment variables
console.log(`Environment: ${JSON.stringify(process.env)}`);

// Handle exit event
process.on("exit", (code) => {
	console.log(`Process exiting with code: ${code}, gracefully`);
});

// Handle uncaught exceptions
process.on("uncaughtException", (err) => {
	console.error("Uncaught exception:", err);
	// process.exit(1); // Exit the process with an error code
});

// Trigger an uncaught exception
throw new Error("This is an uncaught exception");

// Exit the process
process.exit(0);

function step1(callback) {
	process.nextTick(() => {
		callback(new Error("Error in step 1"));
	});
}

function step2(callback) {
	process.nextTick(() => {
		callback(null, "Result from step 2");
	});
}

function main() {
	step1((err) => {
		if (err) {
			return process.nextTick(() => {
				console.error("Error in step :", err.message);
			});
		}

		step2((err, result) => {
			if (err) {
				return process.nextTick(() => {
					console.error("Error in step 2:", err.message);
				});
			}

			console.log("Success:", result);
		});
	});
}

main();

const express = require("express");
const app = express();
const EventEmitter = require("events");
class MyEmitter extends EventEmitter {}

const myEmitter = new MyEmitter();
myEmitter.on("event", () => {
	console.log("An event occurred!");
});
myEmitter.emit("event");
process.on("uncaughtException", (err) => {
	console.error("Uncaught Exception:", err.message);
	// console.error(err.stack);
	// Optionally, you can log the error to a logging service or file

	// It's usually a good idea to restart the process in case of an uncaught exception
	// process.exit(1);
});
process.on("unhandledRejection", (reason, promise) => {
	console.error("Unhandled Rejection at:", promise, "reason:", reason);
	// Perform necessary cleanup
	process.exit(1); // Exit process with failure
});

app.get("/", (req, res) => {
	// Simulate an uncaught exception
	try {
		throw new Error("Something went wrong!");
	} catch (error) {
		console.log(error);
		res.send("fi");
	}
	// throw new Error("Something went wrong 2!");
});
app.get("/promise-rejection", (req, res) => {
	// Create a promise that will be rejected but not caught
	new Promise((resolve, reject) => {
		reject(new Error("This is an unhandled promise rejection!"));
	});

	res.send("This will not handle the promise rejection.");
});

// Example route with proper error handling
app.get("/handled-promise-rejection", (req, res) => {
	new Promise((resolve, reject) => {
		reject(new Error("This promise rejection will be handled."));
	}).catch((err) => {
		console.error("Caught promise rejection:", err.message);
		res.status(500).send("Internal Server Error");
	});
});
app.get("/example", async (req, res, next) => {
	try {
		const result = await someAsyncFunction();
		res.json(result);
	} catch (err) {
		next(err); // Passes the error to the global error handler
	}
});
app.listen(3000, () => {
	console.log("Server is running on port 3000");
});
const validator = require("validator");
const rateLimit = require("express-rate-limit");
const limiter = rateLimit({
	windowMs: 15 * 60 * 1000, // 15 minutes
	max: 100, // limit each IP to 100 requests per windowMs
});
app.use(limiter);
const email = "test@example.com";
if (validator.isEmail(email)) {
	console.log("Valid email");
}
/*
The cluster module allows you to create child processes (workers) that share the same server port.

Stream API for Efficient Data Handling rather than using Buffer

Worker Threads for Parallel Execution: Nodejs worker threads operates independent as that of other worker threads by having their own V8 , libuv instances, event loop & separate heaps



*/

//Singleton

class Singleton {
	constructor() {
		if (!Singleton.instance__) {
			Singleton.instance__ = this;
		}
		return Singleton.instance__;
	}
}

const instance1 = new Singleton();
const instance2 = new Singleton();
console.log(instance1 === instance2); // true
