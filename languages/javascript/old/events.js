const EventEmitter = require("events");
var eventEmitter = new EventEmitter();
function listener() {
	console.log("abd abd");
}
function listener2() {
	console.log("abd abd");
}
function listener3() {
	console.log("once");
}
eventEmitter.on("eventName", listener);
eventEmitter.on("eventName", listener2);
eventEmitter.once("eventName", listener3);
// eventEmitter.off("eventName", listener);
eventEmitter.emit("eventName");
eventEmitter.emit("eventName");


/*
When you register listeners using on(), they are added to the internal array of listeners for the specified event. This happens synchronously 
When you call emit(), it executes the registered listeners for that event. This also happens synchronously, and all listeners are invoked in the order they were registered.
callbacks registered to the EventEmitter are executed as part of the same stack frame in which the emit() method was called
*/



const fs = require("fs");
fs.readFile("/Users/satyambaran/Documents/study/bin/input.txt", function (err, data) {
	if (err) return console.error(err);
	console.log(data.toString());
});
console.log("End of Program execution");

