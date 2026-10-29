class Singleton {
	// Static variable to hold the instance
	static #instance;

	// Private constructor to prevent instantiation from outside
	constructor() {
		if (Singleton.#instance) {
			throw new Error("Instance already exists! Use getInstance() to access the Singleton instance.");
		}
		Singleton.#instance = this; // Set the static instance to the current instance

		// Initialize other properties if needed
		this.someProperty = "Initial Value";
	}

	// Static method to get the Singleton instance
	static getInstance() {
		if (!Singleton.#instance) {
			Singleton.#instance = new Singleton();
		}
		return Singleton.#instance;
	}

	// Example method
	someMethod() {
		console.log(`Current Property Value: ${this.someProperty}`);
	}

	// Example of a method to change the property
	setSomeProperty(value) {
		this.someProperty = value;
	}
}

// Usage
const instance1 = Singleton.getInstance();
instance1.someMethod(); // Output: Current Property Value: Initial Value

const instance2 = Singleton.getInstance();
instance2.setSomeProperty("New Value");
instance1.someMethod(); // Output: Current Property Value: New Value

console.log(instance1 === instance2); // Output: true, both instances are the same
