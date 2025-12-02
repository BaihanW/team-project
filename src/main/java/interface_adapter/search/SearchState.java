package interface_adapter.search;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class SearchState {
    private String locationName;
    private double latitude;
    private double longitude;
    private String searchError;
    private List<String> stopNames = new ArrayList<>();
    private List<GeoPosition> stops = new ArrayList<>();
    private String errorMessage;
    private List<String> suggestions = new ArrayList<>();
    private String suggestionError;

    public SearchState() {}

    public SearchState(SearchState copy) {
        this.locationName = copy.getLocationName();
        this.latitude = copy.getLatitude();
        this.longitude = copy.getLongitude();
        this.searchError = copy.getSearchError();
        this.stopNames = new ArrayList<>(copy.getStopNames());
        this.stops = new ArrayList<>(copy.getStops());
        this.errorMessage = copy.getErrorMessage();
        this.suggestions = new ArrayList<>(copy.getSuggestions());
        this.suggestionError = copy.getSuggestionError();
    }

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

    public List<String> getStopNames() {
        return new ArrayList<>(stopNames);
    }

    public void setStopNames(List<String> stopNames) {
        this.stopNames = new ArrayList<>(stopNames);
    }

    public List<GeoPosition> getStops() {
        return new ArrayList<>(stops);
    }

    public void setStops(List<GeoPosition> stops) {
        this.stops = new ArrayList<>(stops);
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = new ArrayList<>(suggestions);
    }

    public String getSuggestionError() {
        return suggestionError;
    }

    public void setSuggestionError(String suggestionError) {
        this.suggestionError = suggestionError;
    }
}
