/*
•	Dependencies are objects that a class needs to perform its functions.

•	Inversion of Control (IoC): A principle in which the control of object creation and management is transferred from the class itself to an external entity. DI is a concrete implementation of IoC.

Types of Dependency Injection:
	•	Constructor Injection: Dependencies are provided through a class constructor.
	•	Setter Injection: Dependencies are provided through setter methods.
	•	Interface Injection: The dependency provides an injector method that will inject the dependency into any client passed to it.

Benefits of Dependency Injection
	•	Loose Coupling: Classes are less dependent on specific implementations and are instead dependent on abstractions (interfaces), making the system more modular.
	•	Testability: Makes it easier to mock dependencies in unit tests, improving test coverage and reliability.
	•	Flexibility: Easily swap out implementations without changing the dependent class, supporting various configurations and behaviors.
	•	Maintainability: Reduces the need for changes in multiple places when modifying or updating a dependency.


*/