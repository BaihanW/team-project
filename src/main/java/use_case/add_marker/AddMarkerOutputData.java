package use_case.add_marker;

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

