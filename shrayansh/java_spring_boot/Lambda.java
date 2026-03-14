/*
Each class extends from Object class by default. Hence have following methods:
    toString(), getClass(), hashCode(), equals(), clone(), notify(), notifyAll(), wait(), finalize()


Functional Iterface:
    Contains exactly one abstract method. AKA Single Abstract Method (SAM) interface
    It works with and without @FunctionalInterface annotation(w/ annotation it'll throw error if try to add another method)
    Two ways to implement:
        using implements:=
            public class Eagle implements Bird{
                @Override
                public void canFly(){}
            }
        using anonymous class
            Bird bird = new Bird(){
                @Override
                public void canFly(){}
            }
    Types:
        Consumer, Supplier, Function, Predicate
        1. Consumer:
            Accept a single param, cosume it and returns no result
                interface Consumer<T>{void accept(T t);}
                Consumer<Integer> logObj = (Integer val)->{
                    if(val>10) sysout("more than 10");
                    else  sysout("less than 10");
                }
        2. Supplier:
            Accept no input but returns a result
                Supplier<String> supObj = (String str)-> "returning string";
                Supplier<String> supObj = (String str)-> {return "returning string"};
        3. Function:
            interface Function<R,T>{
                R apply (T t);
            }
            Function<String, Integer> intToString = (Integer num) -> {
                return num.toString();
            }
        4. Predicate:
            accept one argument, but always returns boolean
            interface Predicate<T>{
                boolean apply (T t);
            }
            Predicate<Integer> isEven = (Integer num)->{
                return num%2 == 0;
            }
    Inheritance:
        Just need to follow that any functional interface has only method overall
        Even a functional interface can extend other functional interface, if they have same signature

Lambda:
    It's third  way to implement Functional Interface

    Since functional-interface has only one method, then should we to provide that?
    We can utilise the lambda then.

    Functional Interfaces: Lambda expressions implement single abstract methods of functional interfaces.
    Code as Data: Treat functionality as a method argument.
    Class Independence: Create functions without defining a class.
    Pass and Execute: Pass lambda expressions as objects and execute on demand.




*/

@FunctionalInterface
interface Bird {
    void canFly(boolean canFly); // ! only abstract method allowed
    // rest methods can be static, default or from Object class(because it will
    // overridden from by default in each class which a extension of Object class
    // (by default))

    String toString();

    static void canEat() {

    }

    default void getHeight() {

    }

}

public class Lambda {
    public static void main(String[] args) {
        Bird birdObj1 = new Bird() {
            @Override
            public void canFly(boolean canFly) {
                if (canFly)
                    System.out.println("can fly!");
                else
                    System.out.println("can not fly!");
            }

        };
        birdObj1.canFly(true);
        Bird birdObj2 = (boolean canFly) -> {
            if (canFly)
                System.out.println("lambda can fly!");
            else
                System.out.println("lambda can not fly!");

        };
        birdObj2.canFly(false);
    }
}

class k extends Object {

}