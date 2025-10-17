/*
    Abstract: extends
        Can have both implemented method and signature 
        Can have constructor
        Can provide the implementation of interface

    Interface: implements
        Helps interact 2 system without knowing each other all the stuff
        Helps us achieve Abstraction

        Define:
            modifier(public/default) (private and protected not allowed)
            interface interface_name
            comma sepearted parent interfaces
            body
            public interface FlyingBirds extends Bird, LivingThings{
                public void canRun();
            }
            
            Interfaces can only be private/protected, when declared inside a class
                So that, while implementing subclass can select and implement this nested interface
        
        
        Needs:
            It defines WHAT class must do, but not HOW it will Do
                Classes must override the methods of interface
        Polymorphism:
            Multiple sub class can implement a interface by implementing all it's methods
            * Can be used as DataType but can't be used as object
            At the run time it decides which class's method to use
                Bird birdObj1 = new Eagle();
                Bird birdObj2 = new Hen();
                birdObj1.fly();
                birdObj2.fly();
        Multiple Inheritance:
            Is only possible through interface
            Class can not have multiple inhertance from two different classes
                but is for interfaces
                ~ Why?
                    When same method is in both parent class, both of them will have their implementations. Will have to choose one of then, it might break
                        LandAnimal:=canBreathe(){return true;} WaterAnimal:=canBreathe(){return false;}
                    But with interfaces, even if method is present in both interfaces, there implementation will only be in current class
                        Signatures only in interface
                        LandAnimal:=canBreathe(); WaterAnimal:=canBreathe();
        Methods: 
            Implicit public in interfaces
                always public, without mentioning
                can have default, private methods from java8
            Can not be declared as final, because it's signature only must be overridden
        Fields:
            fields are public, static and final implicitly(by default)
                (static and final) := together makes constant
            private are allowed from java9


        Implementation:
            Overriding methods can not be more restrict access specifier
            Concrete class must override all the methods
            Class can implement from multiple interfaces
            Abstract class are not forced to override all the methods
                
                public abstract class Eagle implements Bird
            
            When we use such abstract class to create a concrete class, we need to override all the remaining methods

        Nested Interface:
            In notes
        
        default method:


        



    Transaction management
    Isolation level
    Handle concurrency
    Multi-threading  
    Locking

    There are a few major Java 8 features mentioned below:

        Lambda Expressions: 
            Concise functional code using ->.
        Functional Interfaces: 
            Single-method interfaces.
        Introduced and Improved APIs:
            Stream API: 
                Efficient Data Manipulation.
            Date/Time API: 
                Robust Date and Time Handling.
            Collection API Improvements: 
                Enhanced Methods for Collections (e.g., removeIf, replaceAll).
            Concurrency API Improvements: 
                New classes for parallel processing (e.g., CompletableFuture).
        Optional Class: 
            Handle null values safely.
        forEach() Method in Iterable Interface: 
            Executes an action for each element in a Collection.
        Default Methods: 
            Evolve interfaces without breaking compatibility.
        Static Methods: 
            Allows adding methods with default implementations to interfaces.
        Method References: 
            Refer to methods easily.

    Lambda Expression:
        Functional Interfaces: 
            An interface that contains exactly one abstract method. AKA Single Abstract Method (SAM) interface
            Lambda expressions implement single abstract methods of functional interfaces.
        Code as Data: 
            Treat functionality as a method argument.
        Class Independence: 
            Create functions without defining a class.
        Pass and Execute: 
            Pass lambda expressions as objects and execute on demand.
*/
