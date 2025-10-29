/*
    Static Members:
        Belongs to the class rather than any particular instances. It gets shared by the class.
    Non-Static Members:
        Belongs to a specific instance
    Static methods cant use non-static memebrs:
        because lack of instance contexts

*/
public class Interface {
    public static void main(String[] args) {
        Sedan mySedan = new Sedan();
        mySedan.startEngine(); // Inherited from Car (which overrides Vehicle)
        mySedan.openDoor(); // Implemented in Sedan
        mySedan.stopEngine(); // Implemented in Sedan
        mySedan.honk(); // Overridden in Car (which overrides Vehicle)
        mySedan.playMusic(); // Specific to Sedan
        Vehicle vehicle = new Vehicle() {
            @Override
            public void startEngine() {
                throw new UnsupportedOperationException("Unimplemented method 'startEngine'");
            }

            @Override
            public void stopEngine() {
                throw new UnsupportedOperationException("Unimplemented method 'stopEngine'");
            }
        };
        vehicle.honk();
        System.out.println(vehicle.getClass()); // ! class Interface$1
    }
}

interface Vehicle {
    void startEngine(); // public abstract := these two are added by default

    void stopEngine(); // Abstract method

    default void honk() {
        System.out.println("Honking the horn!");
        // Either child class doesnt touch it then grandchild will have the access of
        // the default method
        // Or child class can abstract it without overriding with the same name
        // Or grandchild can do Child.super.defaultMethod
    }

    static boolean staticMethod() {
        // can not be overridden by classes implementing it
        // can access it using just by interface name as well // Vehicle.staticMethod()
        //
        return true;
    }

    private boolean privateMethod() {
        // these needs to used internally
        // default method can only access it (static can't)
        // it came only after the requirement of default method
        return true;
    }

    private static boolean privateStaticMethod() {
        // these needs to used internally
        // static and default method can only access it
        // from static methods, can only access static memebrs
        return true;
    }

    // ! Nested interface
    interface ElectricVehicle {
        void dashboard();
    }
}

abstract class Car implements Vehicle {
    @Override
    public void startEngine() {
        System.out.println("Car engine started");
    }

    // Abstract method
    public abstract void openDoor();

    // Override the default method from the interface
    @Override
    public void honk() {
        System.out.println("Car horn sounds!");
    }
}

class Sedan extends Car {
    @Override
    public void stopEngine() {
        // ! if you dont override this, will thorw unresolve implementation
        System.out.println("Car engine stopped");
    }

    @Override
    public void openDoor() {
        System.out.println("Sedan door opened");
    }

    // Additional method specific to the Sedan class
    public void playMusic() {
        System.out.println("Playing music in the Sedan");
    }
}

class Tesla implements Vehicle.ElectricVehicle {

    @Override
    public void dashboard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dashboard'");
    }

}

class Both implements Vehicle, Vehicle.ElectricVehicle {

    @Override
    public void dashboard() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dashboard'");
    }

    @Override
    public void startEngine() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startEngine'");
    }

    @Override
    public void stopEngine() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stopEngine'");
    }

}