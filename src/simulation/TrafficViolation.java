package simulation;

import core.Vehicle;

public class TrafficViolation {
    private Vehicle vehicle;
    private String type;
    private int tick;

    public TrafficViolation(Vehicle vehicle, String type, int tick) {
        this.vehicle = vehicle;
        this.type = type;
        this.tick = tick;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getType() {
        return type;
    }

    public int getTick() {
        return tick;
    }
}
