package infrastructure;

public class SpeedBump {
    private double startPosition;
    private double endPosition;
    private double maxAllowedSpeed;

    public SpeedBump(double startPosition, double endPosition, double maxAllowedSpeed) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.maxAllowedSpeed = maxAllowedSpeed;
    }

    public double getStartPosition() {
        return startPosition;
    }

    public double getEndPosition() {
        return endPosition;
    }

    public double getMaxAllowedSpeed() {
        return maxAllowedSpeed;
    }

    public boolean isInside(double position) {
        return position >= startPosition && position <= endPosition;
    }
}

