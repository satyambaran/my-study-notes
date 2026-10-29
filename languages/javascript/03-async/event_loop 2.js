//todo      https://www.geeksforgeeks.org/node-js-event-loop/
//todo 		https://nodejs.org/api/cluster.html
// ` 		https://chatgpt.com/c/3bcba78e-9d58-4440-9d5d-5c6a3044da0e
/*

120		baki ka kaam
100		tiles(seedhi chhod ke)
40		khidki grill
20		bathroom door
10		commode
15		room door
15		boring

80		electricity
90		seedhi
100		putty

*/
setTimeout(function () {
	console.log("This is the third statement");
}, 0);
console.log("This is the first statement");

setTimeout(function () {
	console.log("This is the fourth statement");
}, 1000);

console.log("This is the second statement");

process.nextTick(() => {
	//process.nextTick() is a method that schedules a callback function to be invoked in the next iteration of the event loop, before any I/O operations, timers, or other callbacks are processed
	// process.nextTick() is part of the microtask queue, which runs before the event loop continues to the next phase. Even though process.nextTick is an asynchronous function, its callbacks are prioritized over other types of async operations like timers and setImmediate.
	// Promise have Lower priority than process.nextTick()
	console.log("Next Tick");
});
setImmediate(() => console.log("Set Immediate"));
setTimeout(() => console.log("Set Timeout"), 0);
setImmediate(() => console.log("Set Immediate2")); //? both setTimeout will get executed first, then only setImmediate
setTimeout(() => console.log("Set Timeout2"), 0);
process.nextTick(() => {
	console.log("Process NextTick");
});
/*
   ┌───────────────────────────┐
┌─>│           timers          │
│  └─────────────┬─────────────┘
│  ┌─────────────┴─────────────┐
│  │     pending callbacks     │
│  └─────────────┬─────────────┘
│  ┌─────────────┴─────────────┐
│  │       idle, prepare       │
│  └─────────────┬─────────────┘      ┌───────────────┐
│  ┌─────────────┴─────────────┐      │   incoming:   │
│  │           poll            │<─────┤  connections, │
│  └─────────────┬─────────────┘      │   data, etc.  │
│  ┌─────────────┴─────────────┐      └───────────────┘
│  │           check           │
│  └─────────────┬─────────────┘
│  ┌─────────────┴─────────────┐
└──┤      close callbacks      │
   └───────────────────────────┘

Each box is called a "phase" of the event loop.
Each phase has a FIFO queue of callbacks to execute.

	todo 		timers: this phase executes callbacks scheduled by setTimeout() and setInterval().
	todo 		pending callbacks: executes I/O callbacks deferred to the next loop iteration.
	todo 		idle, prepare: only used internally.
	todo 		poll: retrieve new I/O events; execute I/O related callbacks (almost all with the exception of close callbacks, the ones scheduled by timers, and setImmediate()); node will block here when appropriate.
	todo 		check: setImmediate() callbacks are invoked here.
	todo 		close callbacks: some close callbacks, e.g. socket.on('close', ...).


? If you don't need code to be immediately executed (allowing other callbacks to be invoked), use setImmediate.

`
Event Loop Flow:
•	Executes all synchronous tasks in the call stack.
•	Then processes all pending microtasks from the job queue.
•	Finally, moves to tasks in the callback queue.


!	Stack, Heap, Callback Queue, Event Loop, Job Queue

Heap: Memory Allocation

Javascript call stack:
	moves asynchronous code to callback/event queue after a specified time

Add first() to the stack, run first() which logs 1 to the console, remove first() from the stack.
Add second() to the stack, run second().
Add setTimeout() to the stack, run the setTimeout() Web API which starts a timer and adds the anonymous function to the queue, remove setTimeout() from the stack.
Remove second() from the stack.
Add third() to the stack, run third() which logs 3 to the console, remove third() from the stack.
The event loop checks the queue for any pending messages and finds the anonymous function from setTimeout(), adds the function to the stack which logs 2 to the console, then removes it from the stack.


Javascript message/task queue:
	asynchronous code gets pushed here and waits for the execution.

Whenever the call stack is empty, the event loop will check the queue for any waiting messages, starting from the oldest message. Once it finds one, it will add it to the stack, which will execute the function in the message.
imp: Asynchronous operations, such as I/O operations or timers, are handled by the browser or Node.js runtime. When these operations are complete, corresponding functions (callbacks) are placed in the callback queue.


Event loop: 
Makes sure your asynchronous code runs after all the synchronous code is done executing
It keeps running continuously and checks the Main stack if it has any frames to execute. Ff not then it checks Callback queue, if Callback queue has codes to execute then it pops the message from it and add it to the Main Stack for the execution.

Job Queue:
In Node.js, the job queue is an essential component of the event loop mechanism, used to manage asynchronous tasks and ensure their proper execution. The job queue is sometimes referred to as the microtask queue or the promise queue.
After each phase of the event loop, Node.js checks if the job queue is empty.
If there are microtasks (such as promises or callbacks from certain asynchronous functions) in the job queue, Node.js processes them before moving to the next phase of the event loop.
reserved only for new Promise()
.then() methods are added to Job Queue once the promise has returned/resolved, and then gets executed.
? Job Queue has high priority in executing callbacks, if event loop tick comes to Job Queue, it will execute all the jobs in job queue first until it gets empty, then will move to callback queue.

Callbacks are not asynchronous by nature, but can be used for asynchronous purposes.
A promise represents the completion of an asynchronous function. It is an object that might return a value in the future.
An async function allows you to handle asynchronous code in a manner that appears synchronous. async functions //?still use promises under the hood, but have a more traditional JavaScript syntax.

Once your async code is ready to execute, it will wait for main stack to be empty.
That also means that it is not guaranteed that your setTimeout() or any other async code will run exactly after the time that you have specified. That time is the minimum time after which your code will executed, it can be delayed if Main stack is busy executing existing code.

*/

//! setTimeout inside setTimeout

console.log("Message no. 1: Sync");
setTimeout(function () {
	console.log("Message no. 2: setTimeout");
}, 0);
var promise = new Promise(function (resolve, reject) {
	resolve();
});
promise
	.then(function (resolve) {
		console.log("Message no. 3: 1st Promise");
		let k = new Promise(function (resolve, reject) {
			resolve();
		});
		k.then(() => {
			console.log("Message no. 6: 1st Promise");
			// new Promise(function (resolve, reject) {
			// 	resolve();
			// });
		});
		setTimeout(function () {
			console.log("Message no. 8: setTimeout");
		}, 0);
		k.then(() => {
			console.log("Message no. 7: 1st Promise");
			// new Promise(function (resolve, reject) {
			// 	resolve();
			// });
		});
	})
	.then(function (resolve) {
		console.log("Message no. 4: 2nd Promise");
	})
	.then(function (resolve) {
		console.log("Message no. 7: 3rd Promise");
	});
console.log("Message no. 5: Sync");

// colour codes: 08987D, 086798, 080F98, 6A0898, 98081C, 980808

function fibo(n, m) {
	if (n < 2) {
		// console.trace(n,m); // to see function call stack
		return 1;
	}
	return fibo(n - 1, n) + fibo(n - 2, n);
}
setTimeout(function () {
	console.log("setTimeout");
}, 0);
console.log(fibo(12, 3));
// first 89 and then setTimeout will get printed
// so we better not do any computation heavy work on nodejs server, because it can keep waiting other tasks even though they are done

console.log("Start");
Promise.resolve().then(() => {
	console.log("Promise resolved");
});
setTimeout(() => {
	console.log("setTimeout");
}, 0);
Promise.resolve().then(() => {
	console.log("Promise resolved");
});
console.log("End");
/*
When you call emitter.emit('event'), Node.js immediately invokes all registered listeners for that event synchronously in the same execution context. This means that event emitter callbacks are executed before any microtasks (such as promise callbacks) are processed.
*/
/*

How the Job Queue Works
Event Loop Phases:

When Node.js starts, it initializes an event loop, which consists of multiple phases, including timers, pending callbacks, idle, poll, check, and close handlers.
After each phase of the event loop, Node.js checks if the job queue is empty.
Microtasks Execution:
If there are microtasks (such as promises or callbacks from certain asynchronous functions) in the job queue, Node.js processes them before moving to the next phase of the event loop.
Microtasks are executed in the order they were added to the queue.
Continuation of Event Loop:
After processing all microtasks in the job queue, Node.js continues with the next phase of the event loop.

Use Promises Wisely: Be mindful of how promises are used, as excessive promise chaining can lead to long microtask queues and potential performance issues.

*/

/**!
! package.json

system design>notes on one note

acid, cap, solid

encapsulation, abstraction, iheritance, polymorphism(compile time function overloading and operator overloading, run time(functio overriding, virtual function))



In SQL, changes made by INSERT, UPDATE, or DELETE statements within a transaction are not visible to other transactions until the transaction is committed. Here's a breakdown of how it works:

Inside the Same Transaction: Any changes (insertions, updates, deletions) made within a transaction are immediately visible to subsequent statements within the same transaction. This means you can use the results of these changes in later statements within the same transaction block.

Outside the Transaction: Until the transaction is committed, changes are not visible to other transactions. Other sessions or transactions querying the database will not see the changes until the commit is performed.

Rollback: If the transaction is rolled back, all changes made within that transaction are undone and are never visible to other transactions.

Password security:
Hash passwords: Use strong, adaptive hashing algorithms like bcrypt.
Salt passwords: Ensure each password has a unique salt.
Store securely: Store only hashed passwords and salts, never plain-text passwords.
Secure communication: Use HTTPS to protect data in transit.
Rate limiting: Protect against brute force attacks.
Monitor and audit: Keep track of authentication activities and detect anomalies.

Wrappers in Node.js help abstract and simplify the usage of various libraries and core modules. They can handle repetitive tasks, manage errors, and provide a cleaner API for the rest of your application to interact with.




What is a Process?
A process in Node.js is an instance of the Node.js runtime that executes JavaScript code. It provides an environment with resources such as memory, CPU, and I/O capabilities to run your application.

Key Concepts
Single-Threaded Event Loop:

Node.js operates on a single-threaded event loop architecture, which allows it to handle multiple operations concurrently without creating multiple threads.
The event loop is responsible for managing asynchronous operations. It offloads I/O operations to the system kernel whenever possible, allowing Node.js to perform non-blocking operations efficiently.
Global process Object:

The process object is a global object in Node.js that provides information and control over the current Node.js process.
It can be accessed from anywhere in a Node.js application without requiring any module imports.
Important Properties and Methods of process
Process Information:

process.pid: The process ID.
process.version: The Node.js version.
process.versions: Information about the versions of Node.js and its dependencies.
process.arch: The processor architecture (e.g., 'x64').
process.platform: The platform the Node.js process is running on (e.g., 'linux', 'darwin', 'win32').
Environment Variables:

process.env: An object containing the user environment.
Standard Streams:

process.stdin: A readable stream for standard input.
process.stdout: A writable stream for standard output.
process.stderr: A writable stream for standard error.
Process Control:

process.exit([code]): Exits the process with the specified exit code (default is 0).
process.abort(): Causes the process to emit an abort signal.
process.kill(pid, [signal]): Sends a signal to the process identified by pid.
Event Handling:

process.on('exit', (code) => { ... }): This event is emitted when the process is about to exit.
process.on('uncaughtException', (err) => { ... }): This event is emitted when an exception bubbles all the way back to the event loop.
 */

/*
one call stack per process
v8 places function on call stack
event loop part of libuv


A callback is a function that is passed as an argument to another function and is executed after the completion of a specific task. In Node.js and JavaScript in general, callbacks are commonly used to handle asynchronous operations, enabling non-blocking behavior.

A promise in Node.js is a special object that represents the eventual completion (or failure) of an asynchronous operation and its resulting value. Promises provide a more readable and flexible way to handle asynchronous code compared to traditional callback-based approaches.

*/

const EventEmitter = require("events");
const emitter = new EventEmitter();

console.log("Start of script");

// Register an event listener
emitter.on("event", () => {
	console.log("Event listener"); //! the registered event listeners are executed synchronously in the same execution context. Therefore, Event listener is logged immediately after Start of script.
});
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 0");
});
// NextTick
process.nextTick(() => {
	console.log("process.nextTick");
});
// Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 1");
});
// SetTimeout
setTimeout(() => {
	// Another Promise
	Promise.resolve().then(() => {
		console.log("Promise.resolve.then 8: immediately printed afetr setTimeout due to job queue");
	});
	console.log("setTimeout");
}, 0);
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 2");
});
// SetInterval
const intervalId = setInterval(() => {
	console.log("setInterval");
	clearInterval(intervalId); // Clearing interval after first execution to prevent infinite loop
}, 0);
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 3");
});
// SetImmediate
setImmediate(() => {
	// Another Promise
	Promise.resolve().then(() => {
		console.log("Promise.resolve.then 9: immediately printed afetr setImmediate due to job queue");
	});
	console.log("setImmediate");
});
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 4");
});
// Another NextTick to see the order
process.nextTick(() => {
	console.log("process.nextTick 2");
});
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 5");
});
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 6");
});
// Emit the event
emitter.emit("event");
console.log("End of script");
emitter.emit("event");
// Another Promise
Promise.resolve().then(() => {
	console.log("Promise.resolve.then 7");
});


// Microtask Queue: This queue handles microtasks like promises and process.nextTick(). Microtasks are processed immediately after the currently executing script completes and before the next phase of the event loop.

/*
Execution Order of the Event Loop with Promises

Here’s the overall execution flow during an event loop cycle:

	1.	Execute any currently running JavaScript code (the main script).
	2.	Process the microtask queue, executing all microtasks (like promise callbacks and process.nextTick()).
	3.	Proceed to the timers phase.
	4.	Move to the I/O callbacks phase.
	5.	Execute the poll phase.
	6.	Execute the check phase (running any setImmediate() callbacks).
	7.	Finally, if there are any close callbacks, execute them.
*/
let l = Promise.resolve().then()