const count = Math.random() > 0.5;

let countValue = new Promise(function (resolve, reject) {
	if (count) {
		resolve("greater than 0.5");
		resolve("will not get triggered "); //? can't resolve twice
	} else {
		reject("less than 0.5");
	}
});
try {
} catch (err) {
} finally {
}
countValue
	.then(function (message) {
		console.log(message);
		// return "for next then";
		return new Promise((resolve) => {
			resolve("for next then");
		});
	})
	.then(function (mes) {
		console.log(mes);
		return new Promise((resolve) => {
			resolve("for next then");
		});
		return "success";
	})
	.catch((err) => {
		console.log(err);
		return "fail";
	})
	.finally(function (msg) {
		//! finally callback can not receive any argument
		console.log(msg);
	});

//? order and existence of then, catch and finally doesnt matter (as long as at least one of them is there)

function myDisplayer(anything) {
	console.log(anything);
}
function myCalculator(num1, num2, myCallback) {
	let sum = num1 + num2;
	myCallback(sum);
}
myCalculator(5, 5, myDisplayer);

setTimeout(callbackFun, 300);

function callbackFun() {
	console.log("ToBeExecAfterTimeout");
}

// setInterval(myFunction, 1000);
// function myFunction(){
//     console.log("to be executed after every second")
// }

//? Promise object supports two properties: state and result
// myPromise.state	    myPromise.result
// "pending"	        undefined
// "fulfilled" 	        a result value
// "rejected"	        an error object

let myPromise = new Promise(function (resolve, reject) {
	if (Math.floor(Math.random() * 2)) {
		resolve("greater than 1");
	} else {
		reject("less than 1");
	}
});

myPromise
	.then(function (val) {
		console.log(val);
	})
	.catch(function (val) {
		console.log(val);
	});

myPromise
	.then(
		function (val) {
			console.log(val);
		},
		function (val) {
			console.log("rej", val);
		}
	)
	.catch(function (val) {
		console.log(val);
	}); //todo     .catch wont get executed because second parameter of then will be act as catch

async function asyncFunction() {
	throw "Hello";
	return "Hello";
}
asyncFunction().then(
	function (value) {
		console.log(value);
	},
	function (error) {
		console.log(error, "error");
	}
);

async function useAwait() {
	return await asyncFunction();
}
useAwait()
	.then(function () {
		console.log("sss");
	})
	.catch(function () {
		console.log("fff");
	});

async function asyncReturningPromise(_id) {
	return 5;
	// return new Promise(function (resolve, reject) {
	// 	let success = true;
	// 	let error = "yy";
	// 	if (success) {
	// 		resolve(success);
	// 	} else {
	// 		reject(error);
	// 	}
	// });
}
let k = asyncReturningPromise()
	.then((val) => {
		console.log(val);
		return val;
	})
	.catch((val) => {
		console.log(val);
		return val;
	});

// async makes a function return a Promise

// await makes a function wait for a Promise
// await keyword can only be used inside an async function.
// await keyword makes the function pause the execution and wait for a resolved promise before it continues

const p1 = Promise.resolve(50);
const p2 = new Promise((_resolve, reject) => setTimeout(reject, 100, "geek"));
Promise.allSettled([p1, p2, p1])
	.then((results) => {
		results.forEach((result) => console.log(result.status, result.value, result.reason));
		console.log("");
	})
	.finally(() => console.log("four")); //? A finally callback will not receive any argument
console.log("first");
const p3 = Promise.resolve(40);
const p4 = new Promise((_resolve, reject) => setTimeout(reject, 500, "geek"));
Promise.all([p3, p4])
	.then((results) => {
		console.log(results);
		console.log("third");
	})
	.catch((err) => {
		console.log(err);
		console.log("third");
	});
console.log("second");
// Unlike Promise.all(), Promise.allSettled() does not short-circuit when one of the promises is rejected; instead, it waits for all promises to settle, providing information about each one.

// Method		Description
// then()		Handles a resolve. Returns a promise, and calls onFulfilled function asynchronously
// catch()		Handles a reject. Returns a promise, and calls onRejected function asynchronously
// finally()	Called when a promise is settled. Returns a promise, and calls onFinally function asynchronously

/*
Method							Description
------------------------------|------------------------------
new Promise(executor)			Creates a new Promise.
Promise.all(iterable)			Resolves when all promises in the iterable have resolved or rejects if any promise rejects.
Promise.allSettled(iterable)	Resolves when all promises have settled (resolved or rejected).
Promise.any(iterable)			Resolves when at least one promise fulfills; rejects if all promises are rejected.
Promise.race(iterable)			Resolves or rejects as soon as one promise in the iterable resolves or rejects.
Promise.resolve(value)			Returns a Promise that is resolved with the given value.
Promise.reject(reason)			Returns a Promise that is rejected with the given reason.
then(onFulfilled, onRejected)	Adds fulfillment and rejection handlers.
catch(onRejected)				Adds a rejection handler; returns a promise.
finally(onFinally)				Adds a handler to be called when the promise is settled, regardless of its outcome.
*/

// ====================================================================
// Additional promise examples (from sol.js)
// ====================================================================

// const count = true;

// let countValue = new Promise(function (resolve, reject) {
//     if (count) {
//         resolve("There is a count value.");
//         resolve("There a count value."); //? can resolve a promise only one time
//     } else {
//         reject("There is no count value");
//     }
// });

// countValue
//     .then((message) => {
//         console.log(message);
//     })
//     .then(function (mes) {
//         console.log(mes);
//     })
//     .catch((err) => {
//         console.log(err);
//     })
//     .finally(function (msg) {
//         console.log(msg);
//     });
// ########################################

// let myPromise = new Promise(function (myResolve, myReject) {
//     let x = 0;
//     x = 5;
//     if (x == 0) {
//         myResolve("OK");
//     } else {
//         myReject("Error");
//     }
// });
// function myDisplayer(something = 5) {
//     console.log(something);
// }

// myPromise
//     .catch(function (error) {
//         myDisplayer(error + "hfhdh");
//     })
//     .then(
//         function (value) {
//             myDisplayer(value + "10");
//         },
//         function (error) {
//             myDisplayer(error);
//         }
//     );
// myPromise
//     .then(
//         function (value) {
//             myDisplayer(value);
//         },
//         function (error) {
//             myDisplayer(error);
//         }
//     )
//     .catch(function (error) {
//         myDisplayer(error + "hfhdh");
//     });
// // ########################################

// Example: callback invocation behavior
function myDisplayer2(something = 5) {
	console.log(something);
}

function myCalculator2(num1, num2, funcCall, myCallback) {
	let sum = num1 + num2;
	funcCall;
	funcCall; // will execute only once
	myCallback(sum);
}

myCalculator2(5, 5, myDisplayer2(), myDisplayer2);

// // ########################################

// // setInterval(myFunction, 5000);

// function myFunction() {
//     let d = new Date();
//     console.log(d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds());
// }

// // setTimeout(function () {
// //     myFunction();
// // }, 3000);

// let newPromise = new Promise(function (myResolve, myReject) {
//     setTimeout(function () {
//         myResolve("print something");
//     }, 3000);
// });
// newPromise.then((s) => {
//     myFunction();
// });

// ############################################

// Example: async/await with try-catch
async function foo(id) {
	return new Promise(function (resolve, reject) {
		let success = false;
		let error = "yy";
		if (success) {
			return resolve(success);
		} else {
			return reject(error);
		}
	});
}
async function bar() {
	try {
		var result = await foo("someID");
		console.log(result);
		return result;
	} catch (error) {
		console.log(error);
		return error;
	}
}

// await bar();
bar();
let kk = bar(); //? kk already got executed, but it's a promise to return some value
console.log(kk);

// async function myFunction() {
//     // throw new Error("ttt");
//     return "Hello";
// }
// myFunction()
//     .then(function (value) {
//         console.log(value);
//     })
//     .catch((val) => {
//         console.log(val, "rejected");
//     });
