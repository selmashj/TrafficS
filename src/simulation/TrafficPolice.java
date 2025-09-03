package simulation;

import core.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class TrafficPolice implements Updatable {
    private List<TrafficViolation> detectedViolations;
    private int currentTick;
    private List<Vehicle> vehicles;

    public TrafficPolice(List<Vehicle> vehiclesRef) {
        this.detectedViolations = new ArrayList<>();
        this.vehicles = vehiclesRef;
        this.currentTick = 0;
    }

    public void observe(Vehicle v) {
        double speedLimit = SimulationConfig.DEFAULT_SPEED_LIMIT;
        if (v.getSpeed() > speedLimit) {
            TrafficViolation violation = new TrafficViolation(v, "Speeding", currentTick);
            issueFine(violation);
        }
    }

    public void issueFine(TrafficViolation v) {
        detectedViolations.add(v);
    }

    @Override
    public void update() {
        for (Vehicle v : vehicles) {
            observe(v);
        }
        currentTick++;
    }

    public List<TrafficViolation> getDetectedViolations() {
        return detectedViolations;
    }
}
