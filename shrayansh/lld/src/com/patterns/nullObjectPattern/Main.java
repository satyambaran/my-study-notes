package new_notes.shrayansh.lld.src.com.nullObjectPattern;

/*
 * A null object replaces NULL return type
 * No need to check with if, before calling the method
 * Null objects do nothing or return default behaviour
 * return NullVehicle instead of any vehicle, which will have the default behaviour
 */
public class Main {
    public static void main(String[] args) {
        Vehicle vehicle = VehicleFactory.getVehicleObject("Bike");
        printVehicleDetails(vehicle);
        Vehicle vehicle2 = VehicleFactory.getVehicleObject("Car");
        printVehicleDetails(vehicle2);
    }

    private static void printVehicleDetails(Vehicle vehicle) {
        System.out.println("Seating capacity: " + vehicle.getSeatingCapacity());
        System.out.println("Tank capacity: " + vehicle.getTankCapacity());
    }
}
