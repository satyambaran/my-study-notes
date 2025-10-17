
/*
import java.util.List;

public class Parking {
    public static void main(String[] args) {

    }
}

interface ParkingSpot {
    Integer id;
    boolean isEmpty;
    Vehicle vehicle;
    Integer price;

    void parkVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        isEmpty = false;
    }

    void removeVehicle() {
        this.vehicle = null;
        isEmpty = true;
    }
}

class TwoWheelerSpot implements ParkingSpot {

}

class FourWheelerSpot implements ParkingSpot {

}

class ParkingSpotManager {
    List<ParkingSpot> spots;

    ParkingSpotManager(List<ParkingSpot> spots) {
        this.spots = spots;
    }

    ParkingSpot findParkingSpot() {

    }

    void addParkingSpot() {

    }

    void removeParkingSpot() {

    }
}

class TwoWheelerSpotManager implements ParkingSpotManager {
    TwoWheelerSpot(List<ParkingSpot> spots) {

    }
}

class FourWheelerSpotManager implements ParkingSpotManager {

}

class ParkingSpotManagerFactory {

}

class ParkingStrategy {
    // Near to entrance or lift, default 
}

class Vehicle {
    VehicleType vehicleType;
    String vehicleNum;
}

class Ticket {
    long entryTime;
    ParkingSpot spot;
    Vehicle vehicle;
}

class EntranceGate {
    ParkingSpotManagerFactory factory;
    ParkingSpotManager manager;
    Ticket ticket;

    ParkingSpot findSpot(Vehicle vehicle, int EntranceGateNum) {
    }

    boolean bookSpot(Vehicle vehicle, int EntranceGateNum) {
    }

    Ticket generateTicket(Vehicle vehicle, ParkingSpot spot) {
    }

}
class EntranceGateManager{
}
class ExitGateManager{
}
class ExitGate {
    Ticket ticket;
    CostCompute costCompute;
}

class CostCompute {
    PricingStrategy ps;
    Payment paymentObj;
    ParkingSpotManagerFactory psmf;
    ParkingSpotManager psm;
    Price(){
        ps.price();
    }
    PriceCompute(){

    }
    vacate(){
        removeVehicle()
    }
}
class CostComputeFactory {
   Ticket{
    return TwoWheelerCostCompute or FourWheelerCostCompute based on ticket.vehicle.vehicleType
   }
}

class TwoWheelerCostCompute extends CostCompute {
    super(PricingStrategy ps);
}

class FourWheelerCostCompute extends CostCompute {
    super(PricingStrategy ps);

}

class PricingStrategy {
    int price() {
        return 20;
    }
}

class HourlyPricingStrategy {
    int price;

    int price(Ticket ticket) {
        return ticket.entryTime * price;
    }
}

class MinuteWisePricingStrategy {
    int price;

    int price(Ticket ticket) {
        return ticket.entryTime * price;
    }
}
*/

import java.util.ArrayList;
import java.util.List;

public class Parking {
    public static void main(String[] args) {
        // Simulation of the system
        Vehicle bike = new Vehicle(VehicleType.TWO_WHEELER, "KA01AB1234");
        EntranceGate entranceGate = new EntranceGate();
        ParkingSpot spot = entranceGate.findSpot(bike, 1);

        if (entranceGate.bookSpot(bike, 1)) {
            Ticket ticket = entranceGate.generateTicket(bike, spot);
            System.out.println("Parking Successful for Vehicle: " + bike.vehicleNum + " at Spot ID: " + spot.getId()
                    + ticket.spot);
        } else {
            System.out.println("No spot available for the vehicle.");
        }
    }
}

// Enum for Vehicle Types
enum VehicleType {
    TWO_WHEELER, FOUR_WHEELER;
}

// Parking Spot Interface
interface ParkingSpot {
    Integer getId();

    boolean isEmpty();

    void parkVehicle(Vehicle vehicle);

    void removeVehicle();
}

// Two-Wheeler Spot
class TwoWheelerSpot implements ParkingSpot {
    private Integer id;
    private boolean isEmpty;
    private Vehicle vehicle;

    public TwoWheelerSpot(Integer id) {
        this.id = id;
        this.isEmpty = true;
    }

    public Integer getId() {
        return id;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void parkVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isEmpty = false;
    }

    public void removeVehicle() {
        this.vehicle = null;
        this.isEmpty = true;
    }
}

// Four-Wheeler Spot
class FourWheelerSpot implements ParkingSpot {
    private Integer id;
    private boolean isEmpty;
    private Vehicle vehicle;

    public FourWheelerSpot(Integer id) {
        this.id = id;
        this.isEmpty = true;
    }

    public Integer getId() {
        return id;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void parkVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isEmpty = false;
    }

    public void removeVehicle() {
        this.vehicle = null;
        this.isEmpty = true;
    }
}

// Vehicle Class
class Vehicle {
    VehicleType vehicleType;
    String vehicleNum;

    public Vehicle(VehicleType vehicleType, String vehicleNum) {
        this.vehicleType = vehicleType;
        this.vehicleNum = vehicleNum;
    }
}

// Parking Spot Manager Interface
interface ParkingSpotManager {
    ParkingSpot findParkingSpot();

    void addParkingSpot(ParkingSpot spot);

    void removeParkingSpot(ParkingSpot spot);
}

// Two-Wheeler Parking Spot Manager
class TwoWheelerSpotManager implements ParkingSpotManager {
    List<ParkingSpot> spots;

    public TwoWheelerSpotManager(List<ParkingSpot> spots) {
        this.spots = spots;
    }

    public ParkingSpot findParkingSpot() {
        for (ParkingSpot spot : spots) {
            if (spot.isEmpty()) {
                return spot;
            }
        }
        return null;
    }

    public void addParkingSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public void removeParkingSpot(ParkingSpot spot) {
        spots.remove(spot);
    }
}

// Four-Wheeler Parking Spot Manager
class FourWheelerSpotManager implements ParkingSpotManager {
    List<ParkingSpot> spots;

    public FourWheelerSpotManager(List<ParkingSpot> spots) {
        this.spots = spots;
    }

    public ParkingSpot findParkingSpot() {
        for (ParkingSpot spot : spots) {
            if (spot.isEmpty()) {
                return spot;
            }
        }
        return null;
    }

    public void addParkingSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public void removeParkingSpot(ParkingSpot spot) {
        spots.remove(spot);
    }
}

// Parking Spot Manager Factory (Factory Pattern)
class ParkingSpotManagerFactory {
    public ParkingSpotManager getManager(VehicleType vehicleType, List<ParkingSpot> spots) {
        if (vehicleType == VehicleType.TWO_WHEELER) {
            return new TwoWheelerSpotManager(spots);
        } else {
            return new FourWheelerSpotManager(spots);
        }
    }
}

// Ticket Class
class Ticket {
    long entryTime;
    ParkingSpot spot;
    Vehicle vehicle;

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.entryTime = System.currentTimeMillis();
        this.spot = spot;
        this.vehicle = vehicle;
    }
}

// Entrance Gate
class EntranceGate {
    ParkingSpotManagerFactory factory = new ParkingSpotManagerFactory();
    List<ParkingSpot> twoWheelerSpots = new ArrayList<>();
    List<ParkingSpot> fourWheelerSpots = new ArrayList<>();

    // Add some parking spots
    public EntranceGate() {
        for (int i = 1; i <= 10; i++) {
            twoWheelerSpots.add(new TwoWheelerSpot(i));
        }
        for (int i = 11; i <= 20; i++) {
            fourWheelerSpots.add(new FourWheelerSpot(i));
        }
    }

    ParkingSpot findSpot(Vehicle vehicle, int entranceGateNum) {
        ParkingSpotManager manager = factory.getManager(vehicle.vehicleType,
                vehicle.vehicleType == VehicleType.TWO_WHEELER ? twoWheelerSpots : fourWheelerSpots);
        return manager.findParkingSpot();
    }

    boolean bookSpot(Vehicle vehicle, int entranceGateNum) {
        ParkingSpot spot = findSpot(vehicle, entranceGateNum);
        if (spot != null && spot.isEmpty()) {
            spot.parkVehicle(vehicle);
            return true;
        }
        return false;
    }

    Ticket generateTicket(Vehicle vehicle, ParkingSpot spot) {
        return new Ticket(vehicle, spot);
    }
}

// Exit Gate Class
class ExitGate {
    Ticket ticket;
    CostCompute costCompute;

    public void exit(Ticket ticket) {
        costCompute = CostComputeFactory.getCostCompute(ticket);
        int price = costCompute.Price(ticket);
        System.out.println("Total cost for vehicle: " + ticket.vehicle.vehicleNum + " is: " + price);
        costCompute.vacate(ticket.spot);
    }
}

// CostCompute Factory (Factory Pattern)
class CostComputeFactory {
    public static CostCompute getCostCompute(Ticket ticket) {
        if (ticket.vehicle.vehicleType == VehicleType.TWO_WHEELER) {
            return new TwoWheelerCostCompute(new HourlyPricingStrategy());
        } else {
            return new FourWheelerCostCompute(new MinuteWisePricingStrategy());
        }
    }
}

// CostCompute Class
abstract class CostCompute {
    PricingStrategy ps;

    public CostCompute(PricingStrategy ps) {
        this.ps = ps;
    }

    int Price(Ticket ticket) {
        return ps.price(ticket);
    }

    void vacate(ParkingSpot spot) {
        spot.removeVehicle();
        System.out.println("Spot " + spot.getId() + " is now vacated.");
    }
}

// Two-Wheeler and Four-Wheeler CostCompute Classes
class TwoWheelerCostCompute extends CostCompute {
    public TwoWheelerCostCompute(PricingStrategy ps) {
        super(ps);
    }
}

class FourWheelerCostCompute extends CostCompute {
    public FourWheelerCostCompute(PricingStrategy ps) {
        super(ps);
    }
}

// PricingStrategy Interface and Implementations (Strategy Pattern)
interface PricingStrategy {
    int price(Ticket ticket);
}

// Hourly Pricing Strategy
class HourlyPricingStrategy implements PricingStrategy {
    int price = 20;

    public int price(Ticket ticket) {
        long duration = (System.currentTimeMillis() - ticket.entryTime) / (1000 * 60 * 60); // duration in hours
        return (int) (duration * price);
    }
}

// Minute-Wise Pricing Strategy
class MinuteWisePricingStrategy implements PricingStrategy {
    int price = 1;

    public int price(Ticket ticket) {
        long duration = (System.currentTimeMillis() - ticket.entryTime) / (1000 * 60); // duration in minutes
        return (int) (duration * price);
    }
}