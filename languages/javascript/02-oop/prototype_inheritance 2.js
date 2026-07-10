function Hero(name, level) {
	this.name = name;
	this.level = level;
}
let hero1 = new Hero("Bjorn", 1);
console.log(Object.getPrototypeOf(hero1));

// Adding a method to the constructor
Hero.prototype.greet = function () {
	return `${this.name} says hello.`;
};
console.log(hero1.greet());
function Warrior(name, level, weapon) {
	// Chain constructor with call
	Hero.call(this, name, level); // Warrior constructor wants to inherit properties (name, level) from the Hero constructor.

	// Add a new property
	this.weapon = weapon;
}
/*
Warrior.prototype = Object.create(Hero.prototype);
Warrior.prototype.constructor = Warrior;

if want to inherit methods
*/
function Healer(name, level, spell) {
	Hero.call(this, name, level);

	this.spell = spell;
}
Warrior.prototype.attack = function () {
	return `${this.name} attacks with the ${this.weapon}.`;
};
Healer.prototype.heal = function () {
	return `${this.name} casts ${this.spell}.`;
};
console.log(Warrior.prototype, Healer.prototype);
const hero3 = new Warrior("Bjorn", 1, "axe");
const hero2 = new Healer("Kanin", 1, "cure");
console.log(hero3.attack());
//! when we try to use methods further down the prototype chain?
console.log(hero3.greet?.());
Object.setPrototypeOf(Warrior.prototype, Hero.prototype);
Object.setPrototypeOf(Healer.prototype, Hero.prototype);
console.log(hero3.greet?.());
Hero.prototype.greet2 = function () {
	return `${this.name} says hello.`;
};
console.log(hero3.greet2?.());

class Mage extends Hero {
	constructor(name, level, spell) {
		// Chain constructor with super

		super(name, level); // ` will work
		// Hero.call(name, level); // ` wont work because class must have this, and for this you must call super if its a child class

		// Add a new property
		this.spell = spell;
	}
}
var mage = new Mage("satyam", 1, "sleep");
console.log(mage);

// Every object in JavaScript has a prototype, which is another object from which it inherits properties and methods. This allows JavaScript to implement prototypal inheritance.
// Constructor function
function Person(name, age) {
	this.name = name;
	this.age = age;
}

// Adding a method to the prototype of Person
// Parent constructor
function Animal(name) {
	this.name = name;
}

// Adding a method to the Animal prototype
Animal.prototype.speak = function () {
	return `${this.name} makes a noise.`;
};

// Child constructor
function Dog(name, breed) {
	Animal.call(this, name); // Inherit properties from Animal
	this.breed = breed;
}

// Inherit methods from Animal prototype
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

// Add a method to Dog prototype
Dog.prototype.bark = function () {
	return `${this.name} barks!`;
};

let dog = new Dog("Rex", "German Shepherd");

console.log(dog.speak()); // Output: "Rex makes a noise."
console.log(dog.bark()); // Output: "Rex barks!"

// dog.__proto__ is Person.prototype
console.log(dog.__proto__ === Dog.prototype); // Output: true

function greet(greeting, punctuation) {
	console.log(`${greeting}, my name is ${this.name}${punctuation}`);
}

const person = { name: "Alice" };

// Using call to set 'this' to person
greet.call(person, "Hello", "!"); // Output: "Hello, my name is Alice!"
// Using apply to set 'this' to person with arguments in an array
greet.apply(person, ["Hi", "."]); // Output: "Hi, my name is Bob."
// Using bind to create a new function with 'this' bound to person
const greetPerson = greet.bind(person, "Hey", "!");
greetPerson(); // Output: "Hey, my name is Charlie!"
/*
Method		Execution		Arguments						Returns
call()		Immediate		Passed individually				Return value of the function
apply()		Immediate		Passed as an array				Return value of the function
bind()		Not immediate	Passed individually or preset	New function with bound 
*/
