package use_case.displayRoute;

/**
 * The Input Data for the Display Route Use Case.
 * Represents a route request between two geographical points.
 */
public class DisplayRouteInputData {

    private final double startLatitude;
    private final double startLongitude;
    private final double endLatitude;
    private final double endLongitude;

    public DisplayRouteInputData(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }
}
