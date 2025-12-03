package use_case.add_marker;

public class AddMarkerOutputData {
    private final String locationName;
    private final double latitude;
    private final double longitude;

    public AddMarkerOutputData(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationName() { return locationName; }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
