# **JAVA**

## **Run time vs Compile time**
The differences between **run-time** and **compile-time** are fundamental to understanding how a program is executed. Here's a breakdown:
### 1. **Definition**:
- **Compile-time**: 
	- This is the phase in which the source code is converted into executable code by the compiler. Any errors that occur during this phase are known as compile-time errors.
	- Compile-time events include syntax checking, type checking, and some optimizations.
    - The compiler can only work with the declared types of variables in this phase, while the actual object types and their behaviors are only known when the program is executed.
	
- **Run-time**: 
	- This is the phase when the executable code is actually run on the computer. Any issues that arise during this phase are known as run-time errors.
	- Run-time events involve the actual execution of the program logic, memory allocation, and interaction with the system's hardware and operating system.
    - Can now know actual type of object in this phase. So, Polymorphism and overriding rely on the actual type of an object at runtime, which may not be known during compilation. Therefore, these features require runtime resolution to work correctly.

### 2. **Errors**:
- **Compile-time errors**:
	- These are detected by the compiler before the program runs. Common examples include syntax errors, missing semicolons, undeclared variables, and type mismatches.
	- Example: Trying to assign a string to an integer variable will result in a compile-time error.
        ```java
        int x = "Hello";  // Compile-time error: incompatible types
        ```
    - In this case, the compiler will raise an error because you cannot assign a string to an integer.
	
- **Run-time errors**:
	- These occur while the program is running, after it has been successfully compiled. Common examples include dividing by zero, null pointer exceptions, and out-of-bounds array access.
	- Example: A program trying to access a file that doesn’t exist will encounter a run-time error.
        ```java
        int[] array = new int[5];
        array[10] = 3;  // Run-time error: ArrayIndexOutOfBoundsException
        ```
    - This will compile successfully, but when the program runs, it will crash with an `ArrayIndexOutOfBoundsException` because index 10 is outside the bounds of the array.


### 3. **Binding**:
- **Compile-time binding** (Static Binding):
	- The process of linking method calls to the actual code happens during the compile time. For example, overloading functions, static methods, and method calls on final classes.
	
- **Run-time binding** (Dynamic Binding):
	- This happens when the decision about which method or property to call is made during the execution of the program. This is typically seen in method overriding and polymorphism.
	
### 4. **Performance**:
- **Compile-time**:
	- Errors detected at compile-time are cheaper to fix because they prevent the program from running until resolved.
	
- **Run-time**:
	- Errors detected at run-time can be harder to diagnose, as they might not manifest until specific conditions are met during execution. These can lead to program crashes or unexpected behavior.

### Summary:

|   Aspect              |   Compile-time                                   |   Run-time                                        |
|-----------------------|--------------------------------------------------|---------------------------------------------------|
|   When does it occur? | During compilation of the code.                  | During execution of the program.                  |
|   Errors              | Syntax errors, type errors, etc.                 | Logical errors, memory issues, etc.               |
|   Binding             | Static binding (method overloading).             | Dynamic binding (method overriding).              |
|   Fixing errors       | Easier to fix, caught early by the compiler.     | Harder to debug, may depend on specific inputs.   |
|   Performance         | Faster, as all checks are done before execution. | May introduce delays or crashes during execution. |



## **Key Concepts**
### **1. Static Keyword:**
 - The static keyword in Java is used for memory management. It can be applied to variables, methods, blocks, and nested classes.
 - Static Variables: Shared among all instances of a class (i.e., class-level variable).
 - Static Methods: Can be called without creating an instance of the class.
					They can only access static variables and cannot access non-static members directly.
 - Static Blocks: Used for static initialization of a class, executed when the class is loaded.
### **2. Final Keyword:**
 - The final keyword can be applied to variables, methods, and classes.
 - Final Variables: The value cannot be changed once initialized (i.e., constant).
 - Final Methods: Cannot be overridden by subclasses.
 - Final Classes: Cannot be inherited by any other class.
### **3. Overloading of Static Methods:**
 - Static methods can be overloaded in Java, meaning you can define multiple static methods with the same name but with different parameter lists.
 - However, static methods cannot be overridden, since method overriding is based on dynamic (runtime) binding, while static methods are bound at compile-time.
### **4. Static Binding vs Dynamic Binding:**
 - Static Binding (Early Binding): Occurs at compile time. Static methods and private methods use static binding since they are resolved at compile time.
 - Dynamic Binding (Late Binding): Occurs at runtime. Overriding methods (non-static) use dynamic binding. This allows Java’s polymorphism features to work.
### **5. Extending Multiple Classes in Java:**
 - Java does not support multiple inheritance (i.e., a class cannot extend more than one class) to avoid ambiguity and complexity (Diamond Problem).
 - Instead, Java supports multiple inheritance through interfaces. A class can implement multiple interfaces.


## **Memory in Java**
- 2 types of memory(in RAM) which java creates
	- Heap and Stack
- JVM manages both of them and it's stored in RAM
- Generally Stack have multiple copies and heap has more memory
### Stack vs Heap
- Stack:
	- Mulitple copies (Each thread has their own stack)
	- Each copy of stack can have multiple scope/block
	- Temporary variables
	- Method variables has their own scope
	- Variable within the SCOPE is only visible (any METHOD/BLOCK gets out of scope, it's VARIABLE gets deleted in LIFO order)
	- Premitive data type
	- Reference of the heap objects (object itself gets created in heap)
		- Strong reference (Typical ```Person pObj = new Person();```)
		- Weak reference
			- When garbage collector runs, it'll delete the object because reference is big. Might get null values in this case
			- ```WeakReference<Person> weakPObj = new WeakReference<Person>(new Person());```
		- Soft reference
			- It behaves as Weak only when it's very urgent
			- It behaves as Strong when it's not urgent
	- Stack overflow error (when stack memory gets full)
- Heap:
	- Single instance (Same heap for each thread)
	- Object stays here
	- String pool (String literal stays in string pool which itself in the heap(when we create a new string literal, it checks in memory is its already present if yes pass the refernce thier else creates and then passes the reference))
	- String object stays in Heap. 
	- Bigger than stack
	- Young Generation, Old Generation, Non-heap metaspace(perm-gen)
		1. Young
			- Eden Space
				: When we create any object, it goes in eden
			- S0 Survivor Space
			- S1 Survivor Space
			- Minor GC: Uses Mark & Sweep algorithm
				- will mark unreferenced object from eden and delete it.
				- will push objects from eden to s0
				- updates age by 1
		2. Old Generation
			- Less periodic GC
			- Sweep of minor GC pushes data here once age thresold crosses
		3. Non-heap metaspace (Permanent generation)(earlier it was part of heap b4 v7)
			- class variable (static ones)
			- class metadata
			- final variables
			- GC in Metaspace: Class Unloading

- Garbage Collector:
	- Application will pause, when GC starts
	- When we delete a scope, it's variable gets deleted (that's means only the pointer, object stays in the heap)
	- GC deletes all these unreferenced objects from the heap
	- It runs periodically. JVM controls when to run the garbage collector.
	- Can be executed manually via ```System.gc();```. No guarantee if JVM will run this even after this command.
	- Mark & sweep algorithm
	- Mark & sweep with compact memory algorithm
	- version of GC
		1. Parallel GC (default in java 8)
			- Parallel GC thread working
		2. Serial GC
			- Only one GC thread will be working
		3. Concurrent Mark & Sweep (CMS)
			- Application threads and GC threads are both working concurrently
			- Memory compaction doesnt happen
		4. G1 GC
			- Concurrent with memory compaction


## **Java Methods**
- Method: A block of code designed to perform a task. Can have parameters and a return type.
- Method Declaration: Includes the access modifier, return type, method name, parameters, and method body.
- **Types of Methods**:
	- System-Defined Methods: 	Built-in methods from Java libraries.
	- User-Defined Methods: 	Custom methods created by the developer.
	- Overloaded Methods: 		Methods with the same name but different parameter lists. (overloading is not possible by changing output type)
	- Overridden Methods: 		Methods in a subclass that provide a specific implementation of a superclass method.
	- Static Methods: 			Methods belonging to the class, callable without creating an object.
	- Final Methods: 			Methods that cannot be overridden.
	- Abstract Methods: 		Methods without a body, meant to be implemented by subclasses.
	- **Variable Arguments (Varargs)**: Allows a method to accept a variable number of arguments of the same type.
```java
public void printNumbers(int... numbers) {
    for (int number : numbers) {
        System.out.println(number);
    }
}
// Calling the method with different numbers of arguments
printNumbers(1, 2, 3);
printNumbers(4, 5);
```


## **Java Constructor**
### 1. **What is a Constructor?**

A **constructor** in Java is a special method that is used to initialize objects. Unlike regular methods, constructors have the same name as the class and do not have a return type (not even `void`). Constructors are invoked automatically when an object of the class is created.

Key points:
- A constructor initializes the state of an object.
- It is called when an object is created using the `new` keyword.
- A class can have multiple constructors (constructor overloading).

---

### 2. **Rules of Constructors**

1. **Constructor name must be the same as the class name**.
2. **Constructors do not have a return type**, not even `void`.
3. **Constructors can have parameters** or can be parameterless (no-argument).
4. **They are called automatically when an object is created**.
5. **If no constructor is defined explicitly**, Java provides a default constructor.
6. Constructors can be **overloaded** (multiple constructors with different parameter lists).
7. **Private constructors** can be used to control object creation, often used in Singleton design patterns.
8. **Constructor chaining** can be done, where one constructor calls another constructor.

---

### 3. **Types of Constructors**

#### a. **Default Constructor**
- A **default constructor** is provided by Java when no other constructors are defined in the class.
- It has no parameters and initializes the object with default values (e.g., `null`, `0`, `false`).

#### b. **No-Argument Constructor**
- A **no-argument constructor** is explicitly defined by the developer without any parameters. It initializes the object with default values.

#### c. **Parameterized Constructor**
- A **parameterized constructor** accepts arguments to initialize an object with specific values, rather than using default values.

#### d. **Private Constructor**
- A **private constructor** prevents the creation of objects from outside the class. It is commonly used in Singleton design patterns, where only one instance of a class should exist.
```java
class Singleton {
    private static Singleton instance;

    // Private constructor
    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
Singleton s = Singleton.getInstance();  // Only one instance can be created
```

#### e. **Constructor Overloading**
- **Constructor overloading** allows a class to have multiple constructors with different parameter lists. Based on the arguments passed during object creation, the corresponding constructor is called.
```java
class Person {
    String name;
    int age;

    // No-argument constructor
    public Person() {
        name = "Default Name";
        age = 0;
    }

    // Parameterized constructor
    public Person(String name) {
        this.name = name;
        this.age = 0;
    }

    // Another parameterized constructor
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
Person p1 = new Person();              // Calls no-argument constructor
Person p2 = new Person("Alice");        // Calls constructor with one argument
Person p3 = new Person("Alice", 30);    // Calls constructor with two arguments
```

#### f. **Constructor Chaining**
- **Constructor chaining** occurs when one constructor calls another constructor within the same class or superclass. It is typically done using `this()` to call another constructor in the same class, or `super()` to call a constructor in the superclass.

Example:
```java
class Person {
    String name;
    int age;

    // Constructor with two arguments
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Constructor with one argument, calls the other constructor
    public Person(String name) {
        this(name, 0);  // Calls the constructor with two arguments
    }

    // No-argument constructor, calls the constructor with one argument
    public Person() {
        this("Unknown");  // Calls the constructor with one argument
    }
}
Person p = new Person();  // Constructor chaining is executed
```
---

### 4. **Frequently Asked Interview Questions on Constructors**
1. **Can a constructor be static?**
   - No, a constructor cannot be static. Constructors are used to initialize objects, and static members belong to the class, not instances of the class.
5. **Can a constructor call another constructor?**
   - Yes, a constructor can call another constructor using `this()` for calling within the same class, or `super()` for calling the superclass's constructor.
8. **What is constructor chaining in Java?**
   - Constructor chaining refers to the practice of calling one constructor from another constructor within the same class or calling the superclass constructor from a subclass.
9. **Can a constructor be final?**
   - No, constructors cannot be declared as `final`. The purpose of a constructor is to initialize an object, and it is not intended to be overridden or inherited like methods.

## **Java Classes**
### Types of Classes in Java
```java
	abstract void makeSound();  // Abstract method
	void eat() {}  // Concrete method
```
### 1. **Concrete Class**
- All methods in a concrete class are fully defined.
- Concrete classes can be extended by other classes.
### 2. **Abstract Class**
- Abstract methods are defined using the `abstract` keyword.
- Abstract classes can have both abstract and concrete methods.
- Cannot be instantiated on its own
### 3. **Super Class and Sub Class**
- A **Super Class** (or Parent Class) is a class that is inherited by another class.
- A **Sub Class** (or Child Class) is a class that extends the super class and inherits its properties and behaviors.

### 4. **Object Class**
The **Object class** is the parent class of all classes in Java. Every class in Java implicitly extends `Object`.
- Common methods in `Object` class include `toString()`, `equals()`, `hashCode()`, `getClass()`, and `clone()`.
### 5. **Nested Class**
A **Nested class** is a class within another class. Nested classes can be **static** or **non-static**.

#### a. **Inner Class (Non-static Nested Class)**
An **inner class** is a non-static nested class that is tied to an instance of the outer class.

Example:
```java
class OuterClass {
    class InnerClass {  // Non-static inner class
        void display() {
            System.out.println("This is an inner class");
        }
    }
}
OuterClass.InnerClass inner = new OuterClass().new InnerClass();
inner.display();  // Outputs "This is an inner class"
```

#### b. **Anonymous Inner Class**

An **anonymous inner class** is a class without a name, often used for implementing interfaces or extending classes on the fly.

Example:
```java
Animal animal = new Animal() {  // Anonymous inner class
    void makeSound() {
        System.out.println("Anonymous animal makes a sound");
    }
};
animal.makeSound();  // Outputs "Anonymous animal makes a sound"
```

#### c. **Member Inner Class**

A **member inner class** is a regular class declared inside another class but outside any method.

Example:
```java
class Outer {
    class Inner {  // Member inner class
        void show() {
            System.out.println("Member inner class");
        }
    }
}
Outer.Inner inner = new Outer().new Inner();
inner.show();  // Outputs "Member inner class"
```

#### d. **Local Inner Class**

A **local inner class** is a class declared inside a method or block of code.

Example:
```java
class Outer {
    void method() {
        class LocalInner {  // Local inner class
            void display() {
                System.out.println("Local inner class");
            }
        }
        LocalInner inner = new LocalInner();
        inner.display();
    }
}
new Outer().method();  // Outputs "Local inner class"
```

#### e. **Static Nested Class / Static Class**

A **static nested class** is a nested class that is declared static. It can be instantiated without creating an instance of the outer class.

Example:
```java
class Outer {
    static class StaticNested {  // Static nested class
        void display() {
            System.out.println("Static nested class");
        }
    }
}
Outer.StaticNested nested = new Outer.StaticNested();
nested.display();  // Outputs "Static nested class"
```

---

### 6. **Generic Class**

A **Generic class** allows you to define classes and methods with type parameters, enabling reusability for different data types.

Example:
```java
class Box<T> {  // Generic class
    private T value;
    public void setValue(T value) {
        this.value = value;
    }
    public T getValue() {
        return value;
    }
}
Box<Integer> intBox = new Box<>();
intBox.setValue(10);
System.out.println(intBox.getValue());  // Outputs 10
```
### 7. **POJO Class (Plain Old Java Object)**
A **POJO class** is a simple Java class that contains only fields, getters, setters, and possibly constructors. 
It does not extend or implement any specific classes or interfaces.
Example:
```java
class Person {
    private String name;
    private int age;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
```
### 8. **Enum Class**

An **Enum class** is used to define a collection of constants (fixed set of values). It implicitly extends `java.lang.Enum`.
Example:
```java
enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY;
}

Day today = Day.WEDNESDAY;
System.out.println(today);  // Outputs "WEDNESDAY"
```

---

### 9. **Final Class**

A **final class** cannot be extended or subclassed. This is often used for security and efficiency reasons.

Example:
```java
final class Vehicle {
    void move() {
        System.out.println("Vehicle is moving");
    }
}
// class Car extends Vehicle {}  // This will cause a compile-time error
```

---

### 10. **Singleton Class**

A **Singleton class** ensures that only one instance of the class is created and provides a global point of access to that instance.

Example:
```java
class Singleton {
    private static Singleton instance;

    private Singleton() {}  // Private constructor

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
Singleton singleton = Singleton.getInstance();  // Singleton instance
```

---

### 11. **Immutable Class**

An **Immutable class** is a class whose objects cannot be modified once created. Typically, all fields are declared `final`, and there are no setter methods.

Example:
```java
final class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
Person person = new Person("Alice", 30);  // Immutable object
```

---

### 12. **Wrapper Class**

**Wrapper classes** in Java are used to convert primitive data types into objects. 
The eight wrapper classes are `Integer`, `Long`, `Float`, `Double`, `Character`, `Byte`, `Short`, and `Boolean`.

Example:
```java
int num = 10;
Integer obj = Integer.valueOf(num);  // Wrapping primitive into object
int num2 = obj.intValue();           // Unwrapping object into primitive
```

## **Java Interfaces**
### Interface in Java

---

### 1. **What is an Interface?**

An **interface** in Java is a reference type, similar to a class, that can contain only constants, method signatures, default methods, static methods, and nested types. Interfaces cannot contain instance fields or constructors. They are used to specify a contract that implementing classes must adhere to, allowing for abstraction and multiple inheritance.

### 2. **How to Define an Interface**
You can define an interface using the `interface` keyword. Here’s the syntax:

```java
interface MyInterface {
    void method1();  // Abstract method
    int method2(int param);  // Another abstract method
}
```
### 3. **Why We Need Interfaces**

- **Abstraction**: Interfaces provide a way to achieve abstraction in Java, allowing the user to focus on the "what" rather than the "how."
- **Multiple Inheritance**: Interfaces allow classes to inherit from multiple sources, providing a workaround for Java's restriction on multiple inheritance.
- **Loose Coupling**: Interfaces promote loose coupling by separating the definition of methods from their implementations, making it easier to manage and modify code.
- **Polymorphism**: They enable polymorphic behavior, allowing different classes to be treated as the same type based on the interface they implement.

### 4. **Diamond Problem and Solution**

The **Diamond Problem** occurs when a class inherits from two classes (A and B) that both implement the same interface (C). This can create ambiguity about which implementation of the interface method the subclass should inherit.

**Solution**: In Java, the diamond problem is avoided because:
- Java does not allow multiple inheritance of classes. 
- A class can implement multiple interfaces without ambiguity, and if there are conflicting default methods in interfaces, the class must override the method to provide a specific implementation.

Example:
```java
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}

class C implements A, B {
    @Override
    public void show() {
        A.super.show();  // Explicitly choosing which method to implement
    }
}
```

### 5. **Method in Interface**

- Methods in interfaces are implicitly `public` and `abstract` unless they are defined as `default` or `static`.
- Abstract methods in an interface do not have a body and must be implemented by the classes that implement the interface.

Example:
```java
interface Vehicle {
    void start();  // Abstract method
    void stop();   // Another abstract method
}
```

### 6. **Fields in Interface**

- All fields defined in an interface are implicitly `public`, `static`, and `final`.
- They must be initialized when declared, and cannot be changed afterward.

Example:
```java
interface Constants {
    int MAX_SPEED = 120;  // public static final by default
}
```

### 7. **Interface Implementation and Rules**

- A class implements an interface using the `implements` keyword.
- A class can implement multiple interfaces.
- The implementing class must provide concrete implementations for all abstract methods defined in the interface.

Example:
```java
class Car implements Vehicle {
    @Override
    public void start() {
        System.out.println("Car started");
    }

    @Override
    public void stop() {
        System.out.println("Car stopped");
    }
}
```

---

### 8. **Nested Interface**

#### a. **Interface within Interface**

An interface can contain another interface as a nested interface.

Example:
```java
interface OuterInterface {
    interface InnerInterface {
        void innerMethod();
    }
}
```

#### b. **Interface within a Class**

You can also define an interface within a class.

Example:
```java
class OuterClass {
    interface InnerInterface {
        void innerMethod();
    }
}
```

---

### 9. **Abstract Class vs Interface**

| Feature                       | Abstract Class                      | Interface                        |
|-------------------------------|-------------------------------------|----------------------------------|
| Implementation                | Can have both abstract and concrete methods | Only abstract methods until Java 8, can have default/static methods from Java 8 |
| Fields                        | Can have instance fields            | Only static final fields         |
| Inheritance                   | Supports single inheritance         | Supports multiple inheritance     |
| Constructors                  | Can have constructors               | Cannot have constructors          |
| Access Modifiers              | Can have different access modifiers | Methods are implicitly public     |

---

### 10. **Java 8 and Java 9 Interface Features**

#### a. **Default Method**

- A default method allows you to define a method in an interface with a body. Implementing classes can use this method without needing to provide an implementation.

Example:
```java
interface Vehicle {
    default void honk() {
        System.out.println("Vehicle honking");
    }
}
```

#### b. **Static Method**

- Interfaces can contain static methods that can be called without an instance of the interface.

Example:
```java
interface Vehicle {
    static void printType() {
        System.out.println("Vehicle Type");
    }
}
Vehicle.printType();  // Calling static method
```

#### c. **Functional Interface**

- A functional interface is an interface that has exactly one abstract method. These interfaces can be used as the assignment target for a lambda expression.

Example:
```java
@FunctionalInterface
interface Calculator {
    int add(int a, int b);
}
Calculator calc = (a, b) -> a + b;  // Lambda expression
System.out.println(calc.add(5, 3));  // Outputs 8
```

#### d. **Private Method**

- Java 9 introduced private methods in interfaces that can be used to share common code between default methods.

Example:
```java
interface Vehicle {
    default void start() {
        commonStart();  // Calling private method
    }

    private void commonStart() {
        System.out.println("Common start logic");
    }
}
```

---

### Summary

- **Interface**: A reference type defining a contract for implementing classes.
- **Define an Interface**: Use the `interface` keyword.
- **Need for Interface**: For abstraction, multiple inheritance, and polymorphism.
- **Diamond Problem**: Ambiguity in method inheritance from multiple sources.
- **Method and Fields**: Methods are abstract (unless default/static), fields are public static final.
- **Implementation Rules**: Classes must implement all abstract methods and can implement multiple interfaces.
- **Nested Interface**: Can exist within another interface or class.
- **Abstract Class vs Interface**: Different usage, inheritance, and method definitions.
- **Java 8/9 Features**: Default methods, static methods, functional interfaces, and private methods in interfaces.

## **Reflection**
### Reflection in Java

---

### 1. **What is Reflection?**

**Reflection** is a feature in Java that allows a program to inspect and manipulate classes, interfaces, methods, and fields at runtime, regardless of their access modifiers (private, protected, etc.). It provides the ability to examine or modify the properties of classes, enabling dynamic behavior in applications, such as creating instances, invoking methods, and accessing fields.

### 2. **What is the "Class" Class?**

The `Class` class in Java is part of the Java Reflection API and represents the classes and interfaces in a running Java application. The JVM creates an instance of the `Class` class at runtime for each loaded class and allows for various operations, such as obtaining class metadata.


### 3. **How to Reflect Classes and Access Its Metadata**

You can reflect a class and access its metadata using the `Class` class. Here's how:

```java
// Reflecting a class
Class<?> clazz = MyClass.class;  // Replace MyClass with your class name

// Accessing metadata
System.out.println("Class Name: " + clazz.getName());
System.out.println("Superclass: " + clazz.getSuperclass());
System.out.println("Interfaces: " + Arrays.toString(clazz.getInterfaces()));
System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));
```

---

### 4. **How to Reflect Methods and Access Its Metadata**

To reflect on methods of a class and access their metadata, use the `getMethods()` or `getDeclaredMethods()` methods:

```java
// Reflecting methods
Method[] methods = clazz.getDeclaredMethods();
for (Method method : methods) {
    System.out.println("Method Name: " + method.getName());
    System.out.println("Return Type: " + method.getReturnType());
    System.out.println("Modifiers: " + Modifier.toString(method.getModifiers()));
}
```

### 5. **How to Invoke Methods Using Reflection**

You can invoke a method using reflection by obtaining a `Method` object and calling `invoke()`:

```java
// Assuming there's a method in MyClass named "myMethod" with no parameters
Method method = clazz.getDeclaredMethod("myMethod");
method.setAccessible(true);  // If it's private
Object instance = clazz.getDeclaredConstructor().newInstance();  // Creating an instance
method.invoke(instance);  // Invoking the method
```

---

### 6. **How to Reflect Fields and Access Its Metadata**

To reflect fields of a class and access their metadata, use the `getDeclaredFields()` method:

```java
// Reflecting fields
Field[] fields = clazz.getDeclaredFields();
for (Field field : fields) {
    System.out.println("Field Name: " + field.getName());
    System.out.println("Field Type: " + field.getType());
    System.out.println("Modifiers: " + Modifier.toString(field.getModifiers()));
}
```
### 7. **How to Access and Change the Value of a Public Field**

You can access and change the value of a public field using reflection as follows:

```java
// Accessing a public field
Field publicField = clazz.getField("publicFieldName");  // Replace with actual field name
Object instance = clazz.getDeclaredConstructor().newInstance();
publicField.set(instance, newValue);  // Setting new value
Object value = publicField.get(instance);  // Getting the value
```

---

### 8. **How to Access and Change the Value of a Private Field**

To access and change the value of a private field, you can use the following code:

```java
// Accessing a private field
Field privateField = clazz.getDeclaredField("privateFieldName");  // Replace with actual field name
privateField.setAccessible(true);  // Bypass the access control
privateField.set(instance, newValue);  // Setting new value
Object value = privateField.get(instance);  // Getting the value
```

### 9. **How to Reflect Constructors and Access Its Metadata**

To reflect constructors and access their metadata, you can use the `getDeclaredConstructors()` method:

```java
// Reflecting constructors
Constructor<?>[] constructors = clazz.getDeclaredConstructors();
for (Constructor<?> constructor : constructors) {
    System.out.println("Constructor: " + constructor.getName());
    System.out.println("Modifiers: " + Modifier.toString(constructor.getModifiers()));
}
```

### 10. **How to Access and Invoke Private Constructor Using Reflection**

You can access and invoke a private constructor in the following way:

```java
// Accessing a private constructor
Constructor<?> privateConstructor = clazz.getDeclaredConstructor();
privateConstructor.setAccessible(true);  // Bypass access control
Object instance = privateConstructor.newInstance();  // Creating an instance
```

---

### 11. **How Reflection Breaks Singleton Design Pattern and How to Resolve**

Reflection can break the Singleton design pattern by allowing multiple instances of a class that is designed to have only one instance. By accessing the private constructor via reflection, you can create new instances, violating the Singleton principle.

**Example:**
```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {
        // Private constructor
    }

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}

// Using reflection to break Singleton
Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
constructor.setAccessible(true);
Singleton newInstance = constructor.newInstance();  // Creates a new instance
```

**Solution**: To prevent this, you can throw an exception if an attempt is made to create a second instance in the constructor:

```java
private Singleton() {
    if (instance != null) {
        throw new IllegalStateException("Instance already exists!");
    }
}
```
By incorporating this check, the Singleton class will maintain its integrity even when accessed via reflection.
### Summary
- Class<?>: Represents a class or interface in Java.
- Method: Represents a method of a class.
- Constructor<?>: Represents a constructor of a class.
- Field: Represents a field (member variable) of a class.
- Modifier: Provides static methods and constants to decode class and member access modifiers.


## **Annotations**
- Annotations are a form of metadata in Java
- Annotations are defined using the @interface keyword
- Can read this metadata by using reflection
- Types:
	- Custom
	- Pre-defined
		- Meta-annotations(used on annotations)
			- e.g. ```@Target @Retention @Documented @Inherited @Repeatable```
		- Used on java code (like class, methods, variables, etc)
			- e.g. ```@Deprecated, @Override, @SuppressWarnings, @FunctionalInterface, @SafeVarargs
### Annotations in Java



### 1. **What is Annotation?**

**Annotations** are a form of metadata in Java that provide data about a program but are not part of the program itself. They are used to give additional information to the compiler, tools, or frameworks, which can then use this information for various purposes, such as code analysis, documentation generation, and runtime processing. Annotations are defined using the `@interface` keyword.

### 2. **Pre-defined Annotations**

Java provides several built-in annotations that serve various purposes:

#### - **@Deprecated**

Indicates that a method, class, or field is no longer recommended for use. The compiler generates a warning when this annotation is used.

```java
@Deprecated
public void oldMethod() {
    // Some implementation
}
```

#### - **@Override**

Used to indicate that a method is overriding a method in a superclass. This annotation helps in catching errors at compile-time.

```java
@Override
public void myMethod() {
    // Implementation
}
```

#### - **@SuppressWarnings**

Used to tell the compiler to suppress specific warnings for the annotated element.

```java
@SuppressWarnings("unchecked")
public void myMethod() {
    List rawList = new ArrayList();  // Unchecked warning
}
```

#### - **@FunctionalInterface**

Indicates that an interface is a functional interface, meaning it has exactly one abstract method. This is used primarily in the context of lambda expressions.

```java
@FunctionalInterface
public interface MyFunctionalInterface {
    void execute();  // Single abstract method
}
```

#### - **@SafeVarargs**

Used with variable-length argument methods to indicate that the method does not perform potentially unsafe operations on its varargs parameter.

```java
@SafeVarargs
public static <T> void safeMethod(List<T>... lists) {
    // Implementation
}
```

##### - **Heap Pollution Issue**

Heap pollution occurs when a variable of a generic type refers to an object that is not of that type. The `@SafeVarargs` annotation helps to mitigate this issue in varargs methods.

### 3. **Meta Annotations**
#### - **@Target (Meta-annotation)**

Indicates the types of elements an annotation can be applied to. 
For example, you can use it to restrict an annotation to methods, classes, fields, etc.
```java
@Target(ElementType.METHOD)  // Can be used on methods only
public @interface MyAnnotation {
}
```

#### - **@Retention (Meta-annotation)**
Specifies how long annotations with the annotated type are to be retained. It can have the following values:
- `SOURCE`: Annotations are discarded by the compiler. Not recorded in compiled class file.
- `CLASS`: Annotations are recorded in the class file but not available at runtime.
- `RUNTIME`: Annotations are recorded in the class file and available at runtime through reflection.

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRuntimeAnnotation {
}
```

#### - **@Documented (Meta-annotation)**

Indicates that whenever the annotated element is documented, annotations should be included in the generated documentation.

```java
@Documented
public @interface MyDocumentedAnnotation {
}
```

#### - **@Inherited (Meta-annotation)**

Indicates that an annotation type is automatically inherited. 
If a class is annotated with an inherited annotation, its subclasses will also inherit that annotation.
By default, annotations are not inherited.

```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface MyInheritedAnnotation {
}
```

#### - **@Repeatable (Meta-annotation)**

Allows the same annotation to be applied multiple times to the same declaration.

```java
@Repeatable(Categories.class)  // Container annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	String name();
}

@Retention(RetentionPolicy.RUNTIME)
public @interface Categories {
    Category[] value();
}

```

---

### 4. **Custom/User-Defined Annotations**

You can create your own annotations in Java to provide specific metadata for your application. Here's how you can define a custom annotation:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)  // Can be applied to classes only
@Retention(RetentionPolicy.RUNTIME)  // Available at runtime
public @interface MyCustomAnnotation {
    String value();  // Annotation attribute
    int count() default 1;  // Default value for an attribute
}
```

**Using the Custom Annotation:**

```java
@MyCustomAnnotation(value = "Example", count = 5)
public class MyClass {
    // Implementation
}
```
### Summary
Annotations provide a powerful way to add metadata to your Java programs, allowing for better code organization, readability, and maintainability. Pre-defined annotations serve various purposes, while custom annotations allow developers to define their own metadata needs.


## **Exception Handling**
- Event, that occurs during execution a program
- Disrupts normal program flow
- Creates the Exception object
	- COntains type of exception and message, Stack trace
- If some exception happens, it'll check at current method if it can handle. If not, then it'll check with it's caller. Repeatedly, till it gets handled.
Once you reach main method and even if it can not handle, then runtime system will terminate the program abruptly and print stack trace
### 1. **What is an Exception?**

An **exception** in Java is an event that disrupts the normal flow of the program during its execution. It is an object that is thrown at runtime when an abnormal condition occurs in the code, signaling an error.

Exceptions in Java are classified into two categories: **checked exceptions** and **unchecked exceptions** (also called runtime exceptions).

---

### 2. **Exception Hierarchy**

In Java, exceptions are part of the `java.lang.Throwable` class hierarchy. The hierarchy looks like this:

- **Throwable**: The superclass of all exceptions and errors in Java.
- **Exception**: Represents conditions that a program might want to catch and handle.
- **Error**: Represents serious problems that cannot usually be caught or handled by applications (e.g., hardware failures).

```
java.lang.Object
  |
  +-- java.lang.Throwable
       |
       +-- java.lang.Exception
       |    |
       |    +-- java.lang.RuntimeException (Unchecked)
       |    |      |
       |    |      +-- NullPointerException, ArrayIndexOutOfBoundsException, etc.
       |    |
       |    +-- IOException, SQLException (Checked)
       |
       +-- java.lang.Error (Handled by JVM)
            +-- OutOfMemoryError, StackOverflowError, etc.
```
### 3. **Unchecked (Runtime) Exception**
**Unchecked Exceptions** are exceptions that are not checked at compile-time. They are subclasses of `RuntimeException`. Common examples include:

- `NullPointerException`
- `ArrayIndexOutOfBoundsException`
- `ArithmeticException`

#### Characteristics:
- Do not need to be declared in a method’s `throws` clause.
- Typically indicate programming bugs, such as logic errors.

### 4. **Checked (CompileTime) Exception**

**Checked Exceptions** are exceptions that are checked at compile-time. These are subclasses of `Exception` (but not `RuntimeException`). Common examples include:

- `IOException`
- `SQLException`
- `ClassNotFoundException`

#### Characteristics:
- Must be either caught or declared in the `throws` clause of the method.
- Represent conditions outside the program's control (e.g., file not found, network issues).

---

### 5. **How to Handle Exceptions Properly**

Java provides several keywords to handle exceptions:

- **try**: Defines a block of code that might throw an exception.
- **catch**: Defines how to handle a specific type of exception.
- **finally**: Defines code that will execute regardless of whether an exception is thrown or not.
- **throw**: Used to explicitly throw an exception.
- **throws**: Used to declare that a method might throw one or more exceptions.

#### Example:
```java
public class ExceptionHandlingExample {
    public static void main(String[] args) {
        try {
            int result = divide(10, 0);  // This will throw an exception
        } catch (ArithmeticException e) {
            System.out.println("Exception caught: " + e.getMessage());
        } finally {
            System.out.println("Finally block executed.");
        }
    }

    public static int divide(int a, int b) throws ArithmeticException {
        return a / b;  // May throw ArithmeticException (Unchecked)
    }
}
```

- **try**: Wraps the code that might throw an exception.
- **catch**: Handles the specific `ArithmeticException`.
- **finally**: Always executes, even if no exception was thrown.

---

### 6. **Creating User-Defined Exception Class**

You can create your own exception classes by extending `Exception` (for checked exceptions) or `RuntimeException` (for unchecked exceptions).

#### Example:
```java
class InvalidAgeException extends Exception {
    public InvalidAgeException(String message) {
        super(message);
    }
}

public class CustomExceptionExample {
    public static void main(String[] args) {
        try {
            checkAge(15);
        } catch (InvalidAgeException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }

    public static void checkAge(int age) throws InvalidAgeException {
        if (age < 18) {
            throw new InvalidAgeException("Age must be 18 or older.");
        }
    }
}
```
Here, `InvalidAgeException` is a user-defined exception that is thrown if the age is less than 18.

---

### 7. **Advantages of Exception Handling**

- **Improves Readability**: Exception handling improves the readability of code by separating error-handling code from normal code.
- **Robust Applications**: It allows developers to write more robust applications by handling runtime errors, avoiding unexpected crashes.
- **Error Propagation**: Allows methods to pass exceptions up the call stack to handle them in a centralized manner.
- **Graceful Degradation**: Helps in gracefully handling errors and ensuring that the program doesn't fail abruptly.

---

### 8. **Disadvantages of Exception Handling**

- **Overhead**: Exception handling can introduce performance overhead due to the creation of exception objects and stack traces.
- **Complexity**: Overuse of exceptions can make code more complex and harder to understand.
- **Unchecked Exceptions Ignored**: Since unchecked exceptions are not enforced by the compiler, they may go unhandled, causing runtime errors.
- **Error Suppression**: Improperly handled exceptions can lead to the suppression of critical error information.

---

### Frequently Asked Interview Questions on Exception Handling

1. **What is the difference between checked and unchecked exceptions?**
   - Checked exceptions are checked at compile-time, whereas unchecked exceptions occur at runtime and are not checked during compilation.

2. **Can we catch multiple exceptions in a single catch block?**
   - Yes, from Java 7 onwards, you can catch multiple exceptions in a single `catch` block using the pipe (`|`) operator.
        ```java
        try {
            // Code that may throw multiple exceptions
        } catch (ExceptionType1 | ExceptionType2 | ExceptionType3 e) {
            // Handling logic for all caught exceptions
        }
        ```
3. **What happens if `finally` block throws an exception?**
   - If the `finally` block throws an exception, it will override any exceptions thrown in the `try` or `catch` block. This could result in losing the original exception.

4. **What is the difference between `throw` and `throws`?**
   - `throw` is used to explicitly throw an exception, while `throws` is used in method signatures to declare exceptions that can be thrown.

5. **What is the difference between `final`, `finally`, and `finalize()`?**
   - `final` is a keyword for constants or preventing inheritance/overriding, `finally` is a block that always executes after `try`/`catch`, and `finalize()` is a method called by the garbage collector before object destruction.

By understanding how to handle exceptions properly, you can write more stable and maintainable Java applications!



## **Stream**
 - Stream:
	- Does not store elements.
	- Lazy execution (intermediate operations are executed only when terminal operations are invoked).
	- Designed for functional-style operations.
	- Supports parallel processing.
 - Collection:
	- Stores elements.
	- Eager execution (operations are performed immediately).
	- Used for storing and manipulating data.

After creating stream and operating on it, original data doesnt get changed

1. Step1: Create stream from the data source e.g. collection, array, file
2. Step2: Intermediate operation e.g. filter(), sorted(), map(), distinct() etc.
	- Stream is now converted to another stream. We can have multiple intermediate operations.
3. Step3: Terminal Operations
	- e.g. collect(), reduce(), count() etc
	- It produces final output, means no other operations can be done
```java
import java.util.*;
import java.util.stream.*;

public class StreamExample {
    
    // Static block for initialization (unrelated to streams, but for illustration)
    static {
        System.out.println("Class Loaded! Static Block Initialized.");
    }

    public static void main(String[] args) {
        // Example List
        List<String> names = Arrays.asList("John", "Jane", "Tom", "Harry", "Tim", "Sam", "Mary");

        // 1. Filtering names that start with 'J' and converting to uppercase
        List<String> filteredNames = names.stream()
                .filter(name -> name.startsWith("J"))   // Intermediate Operation (Filter)
                .map(String::toUpperCase)               // Intermediate Operation (Map)
                .collect(Collectors.toList());          // Terminal Operation (Collect)

        System.out.println("Filtered and Mapped Names: " + filteredNames);

        // 2. Find the first name that starts with 'T'
        Optional<String> firstNameStartingWithT = names.stream()
                .filter(name -> name.startsWith("T"))   // Intermediate Operation (Filter)
                .findFirst();                           // Terminal Operation (FindFirst)

        firstNameStartingWithT.ifPresent(name -> System.out.println("First name starting with T: " + name));

        // 3. Collect names into a Set (removes duplicates)
        Set<String> nameSet = names.stream()
                .collect(Collectors.toSet());           // Terminal Operation (Collect)
        System.out.println("Collected Names in Set: " + nameSet);

        // 4. Reducing to concatenate all names
        String concatenatedNames = names.stream()
                .reduce("", (name1, name2) -> name1 + name2); // Terminal Operation (Reduce)
        System.out.println("Concatenated Names: " + concatenatedNames);

        // 5. Count the number of names in the list
        long countNames = names.stream().count();       // Terminal Operation (Count)
        System.out.println("Count of Names: " + countNames);

        // 6. Square and sum numbers using Streams
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        int sumOfSquares = numbers.stream()
                .map(n -> n * n)                        // Intermediate Operation (Map)
                .reduce(0, Integer::sum);               // Terminal Operation (Reduce)
        System.out.println("Sum of Squares: " + sumOfSquares);

        // 7. Parallel Stream Example
        List<String> longNames = names.parallelStream()
                .filter(name -> name.length() > 3)      // Intermediate Operation (Filter)
                .collect(Collectors.toList());          // Terminal Operation (Collect)
        System.out.println("Names with more than 3 characters (Parallel Stream): " + longNames);

        // 8. Using variable arguments (Varargs) with Stream.of
        Stream<String> varargStream = Stream.of("Alpha", "Beta", "Gamma", "Delta");
        varargStream.forEach(System.out::println);       // Terminal Operation (ForEach)

		List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Dave", "Eve");
        System.out.println("Parallel Stream Execution:");
        names.parallelStream().forEach(System.out::println); // May print in any order

        System.out.println("Parallel Stream with forEachOrdered:");
        names.parallelStream().forEachOrdered(System.out::println); // Preserves order
    }
}
```