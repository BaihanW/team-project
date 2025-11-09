package use_case.search;

/**
 * The Input Data for the Search Use Case.
 */
public class SearchInputData {

    private final String locationName;

    public SearchInputData(String locationName) {
        this.locationName = locationName;
    }

    String getLocationName() {
        return locationName;
    }


}
