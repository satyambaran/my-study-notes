// There are four main contexts in which the value of this can be implicitly inferred:
//` 1. the global context
console.log(this);
//` 2. as a method within an object
const america = {
	name: "The United States of America",
	yearFounded: 1776,
	currency: "USD",
	describe() {
		console.log(`${this.name} was founded in ${this.yearFounded}.`);
	},
	d2: function () {
		console.log(`${this.name} was founded in ${this.yearFounded}.`);
	},
	details: {
		symbol: "eagle",
		currency: "Dollar",
		printDetails() {
			console.log(`The symbol is the ${this.symbol} and the currency is ${this.currency}.`);
			console.log(`${this.name} `); //undefined
		},
	},
};
america.describe();
america.d2();
america.details.printDetails();
//` 3. as a constructor on a function or class
function Country(name, yearFounded, name2) {
	this.name = name;
	this.yearFounded = yearFounded;
	this.describe = function () {
		console.log(`${this.name} was founded in ${this.yearFounded}.`);
	};
	this.details = {
		// this.name:name2, //`  this doesnt exist here
	};
}
const india = new Country("The India", 1947);
india.describe();
//` 4. as a DOM event handler
const button = document.createElement("button");
button.textContent = "Click me";
document.body.append(button);
button.addEventListener("click", function (event) {
	console.log(this);
});

//` Arrow functions don’t bind their own this. Instead, they inherit this from their lexical scope (the surrounding code where the arrow function is defined).
("use strict");
function hello() {
	// this; // here this would be k
	console.log(this);
}
hello();
let k = {
	msg: "hi",
	hello,
};
k.hello(); //{ msg: 'hi', hello: [Function: hello] }
let withOut = {
	msg: "hi",
};
let methodWithBindedObject = hello.bind(withOut);
methodWithBindedObject(); // will print the object with which we have bind them i.e. { msg: 'hi' }

function Horse(name) {
	//constructor function
	this.name = name;
	this.sayHello = function () {
		console.log(this.name);
	};
}
let myHorse = new Horse("chetak"); //using the new keyword
// new allows us to create an object where this is automatically bound to newly created object    helps us create methods
myHorse.sayHello();

this.globalObject = "globalObject";
let eg1 = {
	name: "satyam",
	sayHello: function () {
		console.log(this);
	},
	whoAmI: () => {
		console.log(this);
	},
	whoAreYou: () => console.log(this),
};
eg1.sayHello();
eg1.whoAmI(); // global as it doesnt have its bounding to the object and looks at its parent's closing object as this
eg1.whoAreYou();

function show() {
	return this.msg;
}
let obj = {
	msg: "hi",
};
let sayHi = show.bind(obj); // can explicitly set a obj as this on a function
console.log(sayHi());
console.log(show.call(obj)); // same as bind, but without creating seperate object
// functionName.call(thisArg, arg1, arg2, ...);
// functionName.apply(thisArg, [arg1, arg2, ...]);

//operator chaining
function Operator(name) {
	//constructor function
	this.name = name;
	this.sayHello = function () {
		console.log(this.name);
		return this;
	};
}
let operator = new Operator("chaining ");
operator.sayHello().sayHello().sayHello().sayHello();

// Decorator Pattern in JavaScript
function Coffee() {
	this.cost = function () {
		//! this here is compulsory
		return 5;
	};
}
function Milk(coffee) {
	const v = coffee.cost();
	coffee.cost = function () {
		return v + 1;
	};
}
const myCoffee = new Coffee();
Milk(myCoffee);
console.log(myCoffee.cost()); // 6

//
//
//
//
//
//

//& The value of this was determined by its context. However, using call, apply, or bind, you can explicitly determine what this should refer to
// Feature				call	apply		bind
// Invocation		Immediate	Immediate	Delayed
// Arguments	Passed individually	Passed as an array	Predefined and later added
// Return Value	Return value of the function	Return value of the function	New function