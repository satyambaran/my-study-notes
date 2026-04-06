/*
1. Creational Patterns

These patterns deal with object creation mechanisms, trying to create objects in a manner suitable for the situation.

	•	Singleton: Ensures a class has only one instance and provides a global point of access to it.
            Example: Configuration manager.
	•	Factory Method: Defines an interface for creating an object, but allows subclasses to alter the type of objects that will be created.
            Example: Document creation in different formats (PDF, Word).
	•	Abstract Factory: Provides an interface for creating families of related or dependent objects without specifying their concrete classes.
            Example: GUI libraries with different themes.
	•	Builder: Constructs a complex object step by step, allowing for different representations of the object.
            Example: Complex document construction, such as building a computer with various components.
	•	Prototype: Creates new objects by copying an existing object, known as the prototype.
            Example: Cloning objects in a game.

2. Structural Patterns

These patterns deal with object composition and typically help in making complex structures more manageable.

	•	Adapter: Converts the interface of a class into another interface that a client expects.
                Enables interaction between classes that have incompatible interfaces.
            Example: Integrating a legacy system with a new system.
	•	Bridge: Separates an abstraction from its implementation so that the two can vary independently.
            Example: Drawing shapes with different colors.
	•	Composite: Composes objects into tree structures to represent part-whole hierarchies.
            Example: File systems, where folders and files are treated uniformly.
	•	Decorator: Adds additional responsibilities to an object dynamically.
            Example: Adding features to a user interface component.
	•	Facade: Provides a simplified interface to a complex subsystem.
            Example: A simplified API for a complex library.
	•	Flyweight: Reduces the cost of creating and manipulating a large number of similar objects by sharing common parts.
            Example: Text rendering where each character is a separate object but shares common glyphs.
	•	Proxy: Provides a surrogate or placeholder for another object to control access to it.
            Example: Virtual proxy for image loading.

3. Behavioral Patterns

These patterns deal with how objects interact and communicate with each other.

	•	Chain of Responsibility: Passes a request along a chain of handlers, allowing multiple handlers to process the request.
            Example: Logging frameworks with different log levels.
	•	Command: Encapsulates a request as an object, thereby allowing parameterization of clients with queues, requests, and operations.
            Example: Undo functionality in text editors.
	•	Interpreter: Defines a grammar for interpreting sentences in a language and provides an interpreter to interpret sentences of the language.
            Example: Parsing expressions in a programming language.
	•	Iterator: Provides a way to access elements of a collection sequentially without exposing its underlying representation.
            Example: Iterating over collections in Java.
	•	Mediator: Defines an object that encapsulates how a set of objects interact, promoting loose coupling. (to of peer to peer, typically have notify method in it)
            Example: A chat room where users interact through a mediator.
	•	Memento: Allows an object to capture its internal state and restore it later without exposing the details of its implementation.
            Example: Saving the state of a game.
	•	Observer: Defines a dependency between objects so that when one object changes state, all its dependents are notified.
            Example: Event handling systems.
	•	State: Allows an object to change its behavior when its internal state changes.
            Example: State machines, such as a vending machine.
	•	Strategy: Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
            Example: Different sorting algorithms.
	•	Template Method: Defines the skeleton of an algorithm, deferring some steps to subclasses.
            Example: A cooking recipe where specific steps are implemented by subclasses.
	•	Visitor: Represents an operation to be performed on elements of an object structure without changing the classes of the elements.
            Example: Operations on a collection of different types of elements, like calculating taxes or applying discounts.

*/
