
package simulation;

import core.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class StatsManager {
    private List<Vehicle> recordedVehicles;
    private List<TrafficViolation> recordedViolations;

    public StatsManager() {
        recordedVehicles = new ArrayList<>();
        recordedViolations = new ArrayList<>();
    }

    public void recordVehicle(Vehicle v) {
        recordedVehicles.add(v);
    }

    public void recordViolation(TrafficViolation v) {
        recordedViolations.add(v);
    }

    public double getAverageSpeed() {
        if (recordedVehicles.isEmpty()) return 0.0;

        double total = 0;
        for (Vehicle v : recordedVehicles) {
            total += v.getSpeed();
        }
        return total / recordedVehicles.size();
    }

    public int getTotalViolations() {
        return recordedViolations.size();
    }

    public List<Vehicle> getRecordedVehicles() {
        return recordedVehicles;
    }

    public List<TrafficViolation> getRecordedViolations() {
        return recordedViolations;
    }
}






































//package simulation;
//
//import core.Vehicle;
//import law.TrafficViolation;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class StatsManager {
//    private List<Vehicle> recordedVehicles;
//    private List<TrafficViolation> recordedViolations;
//
//    public StatsManager() {
//        recordedVehicles = new ArrayList<>();
//        recordedViolations = new ArrayList<>();
//    }
//
//    public void recordVehicle(Vehicle v) {
//        recordedVehicles.add(v);
//    }
//
//    public void recordViolation(TrafficViolation v) {
//        recordedViolations.add(v);
//    }
//
//    public double getAverageSpeed() {
//        if (recordedVehicles.isEmpty()) return 0.0;
//
//        double total = 0;
//        for (Vehicle v : recordedVehicles) {
//            total += v.getSpeed();
//        }
//        return total / recordedVehicles.size();
//    }
//
//    public int getTotalViolations() {
//        return recordedViolations.size();
//    }
//
//    public List<Vehicle> getRecordedVehicles() {
//        return recordedVehicles;
//    }
//
//    public List<TrafficViolation> getRecordedViolations() {
//        return recordedViolations;
//    }
//}
