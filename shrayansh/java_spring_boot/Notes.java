// 1. Static Keyword
class StaticExample {
    // Static variable (class-level variable)
    static int staticCounter = 0;

    // Non-static variable (instance-level variable)
    int instanceCounter = 0;

    // Static block - executed once when the class is loaded
    static {
        System.out.println("Static block executed.");
        staticCounter = 10;
    }

    // Static method
    static void incrementStaticCounter() {
        staticCounter++;
        System.out.println("Static counter: " + staticCounter);
    }

    // Non-static method
    void incrementInstanceCounter() {
        instanceCounter++;
        System.out.println("Instance counter: " + instanceCounter);
    }
}

// 2. Final Keyword
class FinalExample {
    // Final variable (constant)
    final int CONSTANT = 100;

    // Final method - cannot be overridden
    final void displayConstant() {
        System.out.println("Final variable value: " + CONSTANT);
    }
}

// Final class - cannot be inherited
final class FinalClass {
    void showMessage() {
        System.out.println("This is a final class, cannot be inherited.");
    }
}

// 3. Overloading Static Methods
class StaticMethodOverloading {
    // Overloaded static methods
    static void printMessage() {
        System.out.println("Static method with no arguments.");
    }

    static void printMessage(String message) {
        System.out.println("Static method with a message: " + message);
    }
}

// 4. Static Binding vs Dynamic Binding
class StaticBinding {
    // Static method (static binding)
    static void staticMethod() {
        System.out.println("Static binding: This is a static method.");
    }

    // Non-static method (dynamic binding)
    void dynamicMethod() {
        System.out.println("Dynamic binding: This is a non-static method.");
    }
}

class DynamicBinding extends StaticBinding {
    // Overriding dynamic method (dynamic binding)
    @Override
    void dynamicMethod() {
        System.out.println("Dynamic binding: This method is overridden.");
    }
}

// 5. Extending Multiple Classes in Java (using interfaces)
interface InterfaceA {
    void methodA();
}

interface InterfaceB {
    void methodB();
}

// A class implementing multiple interfaces (Java's way of achieving multiple
// inheritance)
class MultiInheritanceExample implements InterfaceA, InterfaceB {
    @Override
    public void methodA() {
        System.out.println("Method A from InterfaceA.");
    }

    @Override
    public void methodB() {
        System.out.println("Method B from InterfaceB.");
    }
}

public class Notes {
    public static void main(String[] args) {
        // 1. Static Keyword Example
        System.out.println("=== Static Keyword Example ===");
        StaticExample obj1 = new StaticExample();
        StaticExample obj2 = new StaticExample();
        obj1.incrementStaticCounter();
        obj2.incrementStaticCounter();
        obj1.incrementInstanceCounter();
        obj2.incrementInstanceCounter();

        // 2. Final Keyword Example
        System.out.println("\n=== Final Keyword Example ===");
        FinalExample finalEx = new FinalExample();
        finalEx.displayConstant();

        // 3. Overloading Static Methods Example
        System.out.println("\n=== Overloading Static Methods Example ===");
        StaticMethodOverloading.printMessage();
        StaticMethodOverloading.printMessage("Hello, Java!");

        // 4. Static Binding vs Dynamic Binding Example
        System.out.println("\n=== Static Binding vs Dynamic Binding Example ===");
        StaticBinding staticBindObj = new StaticBinding();
        StaticBinding dynamicBindObj = new DynamicBinding();

        // Static method call (static binding)
        StaticBinding.staticMethod();

        // Dynamic method call (dynamic binding)
        staticBindObj.dynamicMethod();
        dynamicBindObj.dynamicMethod();

        // 5. Extending Multiple Classes (Using Interfaces) Example
        System.out.println("\n=== Multiple Inheritance via Interfaces Example ===");
        MultiInheritanceExample multiInherit = new MultiInheritanceExample();
        multiInherit.methodA();
        multiInherit.methodB();
        System.gc();
    }
}


/*
The differences between **run-time** and **compile-time** are fundamental to understanding how a program is executed. Here's a breakdown:

### 1. **Definition**:
- **Compile-time**: 
  - This is the phase in which the source code is converted into executable code by the compiler. Any errors that occur during this phase are known as compile-time errors.
  - Compile-time events include syntax checking, type checking, and some optimizations.
  
- **Run-time**: 
  - This is the phase when the executable code is actually run on the computer. Any issues that arise during this phase are known as run-time errors.
  - Run-time events involve the actual execution of the program logic, memory allocation, and interaction with the system's hardware and operating system.

### 2. **Errors**:
- **Compile-time errors**:
  - These are detected by the compiler before the program runs. Common examples include syntax errors, missing semicolons, undeclared variables, and type mismatches.
  - Example: Trying to assign a string to an integer variable will result in a compile-time error.
  
- **Run-time errors**:
  - These occur while the program is running, after it has been successfully compiled. Common examples include dividing by zero, null pointer exceptions, and out-of-bounds array access.
  - Example: A program trying to access a file that doesn’t exist will encounter a run-time error.

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

### 5. **Examples**:

#### Compile-time Example:
```java
int x = "Hello";  // Compile-time error: incompatible types
```
- In this case, the compiler will raise an error because you cannot assign a string to an integer.

#### Run-time Example:
```java
int[] array = new int[5];
array[10] = 3;  // Run-time error: ArrayIndexOutOfBoundsException
```
- This will compile successfully, but when the program runs, it will crash with an `ArrayIndexOutOfBoundsException` because index 10 is outside the bounds of the array.

### Summary:

|   Aspect              |   Compile-time                                   |   Run-time                                        |
|-----------------------|--------------------------------------------------|---------------------------------------------------|
|   When does it occur? | During compilation of the code.                  | During execution of the program.                  |
|   Errors              | Syntax errors, type errors, etc.                 | Logical errors, memory issues, etc.               |
|   Binding             | Static binding (method overloading).             | Dynamic binding (method overriding).              |
|   Fixing errors       | Easier to fix, caught early by the compiler.     | Harder to debug, may depend on specific inputs.   |
|   Performance         | Faster, as all checks are done before execution. | May introduce delays or crashes during execution. |

 */