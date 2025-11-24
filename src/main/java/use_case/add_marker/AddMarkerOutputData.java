package use_case.add_marker;

/**
 * The output data for the AddMarker use case
 */
public class AddMarkerOutputData {
    private final double latitude;
    private final double longitude;

    public AddMarkerOutputData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}