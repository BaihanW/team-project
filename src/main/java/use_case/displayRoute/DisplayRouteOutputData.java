package use_case.displayRoute;

/**
 * output data for the Display route Use case
 */

public class DisplayRouteOutputData {

    private final double[] longitudes;
    private final double[] latitudes;
    private final double distanceInMeters;
    private final double durationInSeconds;
    private final boolean success;
    private final String message;

    /**
     * creates a new DisplayRouteOutputData
     * @param longitudes array of longitude points along the route
     * @param latitudes array of latitudes points along the route
     * @param distanceInMeters total distance of the route
     * @param durationInSeconds estimated duration of the route
     * @param success whether not the route calculation was successful
     * @param message info or error message
     */

    public DisplayRouteOutputData(double[] longitudes, double[] latitudes,  double distanceInMeters,
                                  double durationInSeconds, boolean success, String message) {
        this.longitudes = longitudes;
        this.latitudes = latitudes;
        this.distanceInMeters = distanceInMeters;
        this.durationInSeconds = durationInSeconds;
        this.success = success;
        this.message = message;
    }

    public double[] getLongitudes() {
        return longitudes;
    }

    public double[] getLatitudes() {
        return latitudes;
    }

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public double getDurationInSeconds() {
        return durationInSeconds;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
