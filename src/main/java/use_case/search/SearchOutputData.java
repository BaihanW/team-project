package use_case.search;

/**
 * Output Data for the Search Use Case.
 */
public class SearchOutputData {
    /**
     * Formal out put data (partial information of Location)
     */
    private final String locationName;
    private final double latitude;
    private final double longitude;

    public SearchOutputData(String locationName, double lat, double lon) {
        this.locationName = locationName;
        this.latitude = lat;
        this.longitude = lon;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
