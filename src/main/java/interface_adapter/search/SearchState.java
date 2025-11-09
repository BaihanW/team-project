package interface_adapter.search;

/**
 * The state for the Search Model.
 */
public class SearchState {
    private String locationName;
    private double latitude;
    private double longitude;
    private String searchError;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getSearchError() {
        return searchError;
    }

    public void setSearchError(String searchError) {
        this.searchError = searchError;
    }

}
