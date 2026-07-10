// ` A JavaScript class is a type of function
var x = function () {};
var y = class {};
var a = new x();
var a = new y();

// A constructor function is initialized with a number of parameters, which would be assigned as properties of this, referring to the function itself. The first letter of the identifier would be capitalized by convention.

// //? class, function and new
class Car {
	constructor(make, model, year) {
		this.make = make;
		this.model = model;
		this.year = year;
	}
	a = 8; // matters
	#b = 8; // private property
	// Adding a method to the constructor
	greet() {
		return `${this.name} says hello ${this.#b}.`;
	}
	_internalMethod() {
		console.log("internal method");
	}
}
function Car2(make, model, year) {
	this.make = make;
	this.model = model;
	this.year = year;
	// return this;
	a = 8; // doesnt matter
}

// Adding a method to the constructor
Car2.prototype.greet = function () {
	return `${this.name} says hello.`;
};
const car1 = new Car("Eagle", "Talon TSi", 1993); //! since it's called with `new` keayword, function will act as a constructor
console.log(car1, car1.a);
car1._internalMethod()
// console.log(car1, car1.#b);
const car2 = new Car("Eagle");
console.log(car2);

// function Cycle(make, model, year) {
// 	this.make = make;
// 	this.model = model;
// 	this.year = year;
// 	return 5;
// }
// const cycle1 = new Cycle("Eagle", "Talon TSi", 1993);
// console.log(cycle1); // Cycle { make: 'Eagle', model: 'Talon TSi', year: 1993 }

// const cycle2 = Cycle("Eagle", "Talon TSi", 1993);
// console.log(cycle2); // 5

// function Person(name, age, sex) {
// 	this.name = name;
// 	this.age = age;
// 	this.sex = sex;
// }
// const rand = new Person("Rand McNally", 33, "M");
// const ken = new Person("Ken Jones", 39, "M");
// function Bike(make, model, year, owner) {
// 	this.make = make;
// 	this.model = model;
// 	this.year = year;
// 	this.owner = owner;
// }
// const bike1 = new Bike("Eagle", "Talon TSi", 1993, rand);
// const bike2 = new Bike("Nissan", "300ZX", 1992, ken);
// console.log(bike1, bike2, bike2.owner.name);

class Man {
	constructor(name) {
		//todo    The constructor method is called automatically when a new object is created.
		this.name = name;
	}
	greet() {
		console.log(`Hello, my name is ${this.name}`);
	}
}

const p = new Man("Satyam"); //todo can not be invoked without new
p.greet(); // Hello, my name is Satyam
// //!
// //*
// //?
// //todo

class Car {
	/**
	 * desc
	 * @date 2023-02-22
	 * @param { * } brand
	 */
	constructor(brand) {
		this.carname = brand;
	}
	/**
	 * desc
	 * @date 2023-02-22
	 */
	present() {
		return "I have a " + this.carname;
	}
}

class Model extends Car {
	//? extends keyword is used to create a child class, which  inherits all the methods from  prent
	/**
	 * desc
	 * @date 2023-02-22
	 * @param { * } brand
	 * @param { * } mod
	 */
	constructor(brand, mod) {
		super(brand); //?  super keyword is used to call the constructor of its parent class to access the parent's properties and methods.

		//? If there is a constructor present in the subclass, it needs to first call super() before using "this" to get parents' attribute

		this.model = mod;
	}
	/**
	 * desc
	 * @date 2023-02-22
	 */
	show() {
		return this.present() + ", I repeat, I have a " + this.carname + ", it is a " + this.model + this.show;
	}
}
// class SubClass extends Model, Man{ //? Classes can only extend a single class
class SubClass extends Model {
	constructor(a, b, c) {
		super(a, b);
		this.c = c;
	}
	d = this.c; //? undefined
	e = function () {};
}
let subClass = new SubClass("a", "b", "c");
console.log(subClass);
let myCar = new Model("Ford", "Mustang");
console.log(myCar.show());

class Rectangle {
	height = 0;
	width;
	#height = 0; // private
	#width;
	/**
	 * desc
	 * @date 2023-02-22
	 * @param { * } height
	 * @param { * } width
	 */
	constructor(height, width) {
		this.#height = height;
		this.#width = width;
	}
}
/*

Feature			Function							Class
Use Case		General-purpose, modular logic		Object-oriented programming
Instantiation	Returns an object manually			Uses new keyword
Syntax			Lightweight, flexible				Structured, verbose
Inheritance		Achieved via prototypes				Achieved via extends
Private Members	Not supported natively				Supported using #privateFields
				(until ES2020)
This Binding	Needs explicit binding for context	Implicit in methods of the class
				(bind, call, apply)
Code Style		Functional programming				Object-oriented programming

*/
// console.log(029=="029")
// console.log(028=="028")
// console.log(027=="027")
// console.log(026=="026")
// console.log(025=="025")
// console.log(024=="024")
// console.log(023=="023")
// console.log(022=="022")
// console.log(021=="021")
// console.log(020=="020")
// console.log(019=="019")
// console.log(018=="018")
// console.log(017=="017")
// console.log(016=="016")
// console.log(015=="015")
// console.log(014=="014")
// console.log(013=="013")
// true
// true
// false
// false
// false
// false
// false
// false
// false
// false
// true
// true
// false
// false
// false
// false
// false


// call() copies only properties (not methods) from the parent constructor.

// ====================================================================
// Static methods example (from sol.js)
// ====================================================================

// class Car {
// 	constructor(name) {
// 		if (Car.inst) return this;
// 		this.name = name;
// 		Car.inst = true;
// 	}
// 	static hello(x) {
// 		//todo Static properties cannot be directly accessed on instances of the class. Instead, they're accessed on the class itself.
// 		return "Hello " + x.name;
// 	}
// 	static inst = false;
// }
// let myCar = new Car("Ford");
// console.log(Car.hello(myCar));
// console.log(myCar.hello()); //wont work

// ====================================================================
// Getter/Setter example (from sol.js)
// ====================================================================

// class Car {
//     constructor(brand) {
//         this._carname = brand;
//     }
//     get carname() {
//         return this._carname;
//     }
//     set carname(x) {
//         this._carname = x;
//     }
// }

// let myCar = new Car("Ford");
// console.log(myCar._carname, myCar.carname);

// ====================================================================
// Inheritance example (from sol.js)
// ====================================================================

// class Car {
//     constructor(brand) {
//         this.carname = brand;
//     }
//     present() {
//         return "I have a " + this.carname;
//     }
// }

// class Model extends Car {
//     constructor(brand, mod) {
//         super(brand);
//         this.model = mod;
//     }
//     show() {
//         return (
//             this.present() +
//             ", I repeat, I have a " +
//             this.carname +
//             ", it is a " +
//             this.model +
//             this.show
//         );
//     }
// }

// let myCar = new Model("Ford", "Mustang");
// console.log(myCar.show());