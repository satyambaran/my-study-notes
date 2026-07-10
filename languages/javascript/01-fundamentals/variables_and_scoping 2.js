// A computer program is a list of /* instructions to be executed */ by a computer.
// In a programming language, these programming instructions are called statements.
// A JavaScript program is a list of programming statements.

// !const, let & var

//? var is function scoped when it is declared within a function
// var is global scoped when declared outside of function
// Keyword	Scope		Hoisting	Reassignable	Redeclarable	Temporal Dead Zone (TDZ)
// var		Function	Yes			Yes				Yes				No
// let		Block		Yes			Yes				No				Yes
// const	Block		Yes			No				No				Yes
/*
Hoisting refers to the behavior where variable and function declarations 
are moved to the top of their containing scope during the compilation phase. 
However, only the declarations are hoisted, not the initializations. 
This means:
	~ Variables declared with `var` are hoisted and initialized with undefined.
	~ Let and const are hoisted but not initialized, causing a “temporal dead zone.”
	~ Function declarations are fully hoisted, so they can be called before their definition in the code.
*/
var tester = "hey hi";
function newFunction() {
	var hello = "hello";
}
//~ console.log(hello); // error: hello is not defined

//* var variables can be re-declared and updated in the same scope
var greeter = "declared";
var greeter = "re-dclared";
greeter = "updated";

var carName = "Volvo";
var carName;
console.log("carName", carName);

// Hoisting is a JavaScript mechanism where variables and function declarations are moved to the top of their scope before code execution.
// so
console.log(greeter);
var greeter = "say hello";
// is equivalent of this
var greeter;
console.log(greeter); // greeter is undefined
greeter = "say hello";

//? let is block scoped, which can be updated but not re-declared

//~ console.log(msg); // Cannot access 'msg' before initialization
let msg = "let doesnt work this way";

//? const declarations are block scoped which cannot be updated or re-declared
const greeting = "say Hi";
//~ greeting = "say Hello instead";// error: Assignment to constant variable.
//~ const greeting = "say Hello instead";// error: Identifier 'greeting' has already been declared
// const PI; //'const' declarations must be initialized.ts(1155)

//? CAN: Change the elements of constant array
//? CAN: Change the properties of constant object
const cars = ["Saab", "Volvo", "BMW"];
cars[0] = "Toyota";
cars.push("Audi");
console.log(cars);
// cars = ["Toyota", "Volvo", "Audi"];    // ERROR
let k = cars.pop();
console.log(k);

const constExample = {
	message: "say Hi",
	times: 4,
};
// constExample = {
// 	//~ Assignment to constant variable. Not Allowed
// 	words: "Hello",
// 	number: "five",
// };

constExample.number = "five";
constExample.times = { week: 4 };
delete constExample.message;
console.log(constExample);

//? const let & var are all hoisted to the top of their scope

let str = "11";
console.log(
	str + "1", // 111
	str + 1, // 111
	str - 1, // 10
	str * 2, // 22
	str + str, // 1111
	2 * str, //22
	"5" + 2 + 3, //523
	2 + "5" + 3 //253
);

console.log("1" === 1, "1" !== 1, "1" == 1, "1" != 1);
// false true true false    strtict equality/inequality

let num = 5;
console.log(3 ** 2, ++num, num++, num, --num, num--, num);
let text = "happy ";
text += "new year";
console.log(text);

//? null : null value represents the absence of any object value //treated as false
console.log(typeof null, typeof undefined, null == false);

const a = { duration: 50 };
a.speed = a.speed ?? 25; //* nullish coalescing, check if null or undefined
a.duration = a.duration ?? 10;
console.log(a.duration, a.speed);
const foo = null ?? "default string";
console.log(foo);

// https://developer.mozilla.org/en-US/docs/Web/JavxaScript/Reference/Operators/Object_initializer

this.name = "global_this";
const adventurer = {
	name: "Alice",
	cat: {
		name: "Dinah",
	},
	fun: (val) => {
		console.log("fun is printing", val, this.name);
		//? 'this' doesn't work in arrow function
	},
	fullName: function () {
		return this.name;
	},
};
const dogName = adventurer.dog?.name; // Operator chaining
let fun = adventurer.fun;
console.log(dogName);
console.log(dogName, adventurer.fun?.(5), fun?.(5), adventurer.someNonExistentMethod?.());

// The () Operator Invokes the Function

let nameObj = {
	certName: "Satyam",
	homeName: "Kundan",
	surname: "Barnwal",
};
let strList = ["satyam", "kumar", "barnwal"];
console.log(strList?.[4], strList?.includes("kundan"), nameObj?.["certName"]);

//? Datatypes
// String, Number, Bigint, Boolean, Undefined, Null, Symbol, Object(object, array, Date)

let x = new String("John"); // x is an object
let y = new String("John"); // y is an object
let z = "John";
console.log(x == y, y == z, x == z);
//? Comparing two JavaScript objects always returns false.
x = {
	f: "u",
	s: "u",
};
y = {
	f: "u",
	s: "u",
};
console.log(x == y); //  false

console.log(Math.random());

let xss = (![] + [])[+[]] + (![] + [])[+!+[]] + ([![]] + [][[]])[+!+[] + [+[]]] + (![] + [])[!+[] + !+[]];
console.log(xss);

// JS concurrency: https://www.youtube.com/watch?v=-JE8P2TiJEg&ab_channel=BeyondFireship
// https://www.youtube.com/watch?v=_Im4_3Z1NxQ&t=245s&ab_channel=CodeWithRyan
// https://www.youtube.com/watch?v=vn3tm0quoqE&t=111s&ab_channel=Fireship
// https://www.youtube.com/watch?v=Mus_vwhTCq0&ab_channel=Fireship

var promiseAll = function (functions) {
	return new Promise((res, rej) => {
		if (functions.length == 0) {
			res([]);
		}
		const ret = new Array(functions.length).fill(null);
		let tot = 0;
		functions.forEach(async (ele, idx) => {
			try {
				ret[idx] = await ele();
				tot++;
				if (tot == functions.length) res(ret);
			} catch (err) {
				rej(err);
			}
		});
	});
};
var functions = [() => new Promise((res, rej) => res(5))];
// functions.forEach((ele, idx)=>{

// })
var nm = "Asheesh";
console.log(nm);
function sayHi() {
	// var nm="amit"
	console.log(nm);
	hi();
	function hi() {
		console.log(nm);
	}
}
sayHi();

// Initializing a new string primitive
const stringPrimitive = "A new string.";
console.log(typeof stringPrimitive); //` string
// Initializing a new String object
const stringObject = new String("A new string.");
console.log(typeof stringObject); //` object

// Initialize a global variable
var species = "human";

function transform() {
	// Initialize a local, function-scoped variable
	console.log(species); //` undefined
	var species = "werewolf";
	console.log(species);
}
// Log the global and local variable
// console.log(species);
transform();
console.log(species); //` humAN
if (true) {
	var species = "werewolf";
	console.log(species);
}
console.log(species); //` werewolf

// Keyword		Scope				Hoisting	Can Be Reassigned		Can Be Redeclared
// var			Function scope		Yes			Yes						Yes
// let			Block scope			No			Yes						No
// const		Block scope			No			No						No

const CAR = {
	// ` const itslef cant be assigned but can add and edit properties
	color: "blue",
	price: 15000,
};
CAR.price = 20000;
CAR.speed = 300;
delete CAR["color"];
console.log(CAR);

var gimli = new Object();
var gimli = {
	name: "Gimli",
	race: "dwarf",
	weapon: "axe",
	greet: function () {
		return `Hi, my name is ${this.name}!`;
	},
};
console.log(gimli["greet"]());
for (let key in gimli) {
	console.log(gimli[key]);
}
Object.keys(gimli);

let ob = {};
console.log(Object.getPrototypeOf(ob));
console.log(ob.__proto__);

console.log(1);
new Promise((resolve) => {
	setTimeout(() => {
		console.log(2);
		resolve();
	}, 1000);
})
.then(() => {
	return new Promise((resolve) => {
		setTimeout(() => {
			console.log(3);
			resolve();
		}, 0);
	});
})
.then(() => {
	console.log(4);
});
