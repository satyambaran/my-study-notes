"use strict";
// console.log("hello");
// tsc --init   //to generate 'tsconfig.json' file
// tsc          //to convert .ts file to .js file
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
