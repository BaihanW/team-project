package interface_adapter.search;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.search.SearchOutputBoundary;
import use_case.search.SearchOutputData;

import java.util.List;

public class SearchPresenter implements SearchOutputBoundary {

    private final SearchViewModel searchViewModel;

    public SearchPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void prepareSuccessView(SearchOutputData response) {
        final SearchState searchState = new SearchState(searchViewModel.getState());

        // Add stop
        List<String> names = searchState.getStopNames();
        names.add(response.getLocationName());
        searchState.setStopNames(names);
        List<GeoPosition> stops = searchState.getStops();
        stops.add(new GeoPosition(
                response.getLatitude(),
                response.getLongitude()
        ));
        searchState.setStops(stops);

        // Update location info
        searchState.setLatitude(response.getLatitude());
        searchState.setLongitude(response.getLongitude());
        searchState.setLocationName(response.getLocationName());
        searchState.setSuggestions(List.of());
        searchState.setSuggestionError(null);

        searchViewModel.setState(searchState);
        searchViewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String error) {
        final SearchState searchState = new SearchState(searchViewModel.getState());
        searchState.setSearchError(error);
        searchState.setSuggestionError(null);
        searchViewModel.setState(searchState);
        searchViewModel.firePropertyChange();
    }
}
