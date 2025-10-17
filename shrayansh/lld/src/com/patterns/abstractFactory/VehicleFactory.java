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
    public Vehicle getVehicle(VehicleType vehicleType) {
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

class LuxuryVehicleFactory extends VehicleFactory {

    @Override
    public Vehicle getVehicle(VehicleType vehicleType) {
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