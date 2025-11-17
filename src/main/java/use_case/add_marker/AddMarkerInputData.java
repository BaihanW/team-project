package use_case.add_marker;

public class AddMarkerInputData {
    private final double latitude;
    private final double longitude;

    public AddMarkerInputData(double latitude, double longitude) {
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
