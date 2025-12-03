package interface_adapter.addMarker;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.add_marker.AddMarkerOutputBoundary;
import use_case.add_marker.AddMarkerOutputData;

import java.util.ArrayList;
import java.util.List;

public class AddMarkerPresenter implements AddMarkerOutputBoundary {
    private final SearchViewModel searchViewModel;

    public AddMarkerPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void prepareSuccessView(AddMarkerOutputData outputData) {
        SearchState currentState = searchViewModel.getState();
        SearchState newState = new SearchState(currentState);

        List<String> currentNames = newState.getStopNames();
        currentNames.add(outputData.getLocationName());
        newState.setStopNames(currentNames);

        List<GeoPosition> currentStops = newState.getStops();
        currentStops.add(new GeoPosition(outputData.getLatitude(), outputData.getLongitude()));
        newState.setStops(currentStops);
        newState.setLatitude(outputData.getLatitude());
        newState.setLongitude(outputData.getLongitude());
        newState.setLocationName(outputData.getLocationName());

        searchViewModel.setState(newState);
        searchViewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String error) {
        SearchState newState = new SearchState(searchViewModel.getState());
        newState.setErrorMessage(error);
        searchViewModel.setState(newState);
        searchViewModel.firePropertyChange();
    }
}