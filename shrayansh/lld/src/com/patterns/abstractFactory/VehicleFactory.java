package new_notes.shrayansh.lld.src.com.abstractFactory;

import com.abstractFactory.BMW;
import com.abstractFactory.Hyundai;
import com.abstractFactory.Mercedes;
import com.abstractFactory.Swift;
import com.abstractFactory.Vehicle;
import com.abstractFactory.VehicleType;

public abstract class VehicleFactory {
    public abstract Vehicle getVehicle(VehicleType vehicleType);
}

class OrdinaryVehicleFactory extends VehicleFactory {

    @Override
    public OrdinaryVehicle getVehicle(VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }
        switch (vehicleType) {
            case HYUNDAI:
                return new Hyundai();
            case SWIFT:
                return new Swift();
            default:
                return null;
        }
    }
}

class Vehicle{
    Drive drive;
    Vehicle(){
        drive = getDrive();
    }
    getDrive();
}

Hyundai extends Vehicle{
    Drive drive;
    Drive getDrive(){
        return new NormalDrive();
    }
}

BMW extends Vehicle{
    Drive drive;
    Drive getDrive(){
        return new BMWDrive();
    }
}

// we call a function to create the object rather than calling the constructor directly for that class
// We can do it based on some conditions which returns different sub classes of the same parent class

class OrdinaryVehicle extends Vehicle{
    commonProperty1
}
class LuxuryVehicle extends Vehicle{
    commonProperty2
}

class LuxuryVehicleFactory extends VehicleFactory {

    @Override
    public LuxuryVehicle getVehicle(VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }
        switch (vehicleType) {
            case BMW:
                return new BMW();
            case MERCEDES:
                return new Mercedes();
            default:
                return null;
        }
    }
}