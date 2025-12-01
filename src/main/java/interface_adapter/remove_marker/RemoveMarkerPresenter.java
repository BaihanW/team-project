package interface_adapter.remove_marker;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import use_case.remove_marker.RemoveMarkerOutputBoundary;
import use_case.remove_marker.RemoveMarkerOutputData;

public class RemoveMarkerPresenter implements RemoveMarkerOutputBoundary {

    private final SearchViewModel searchViewModel;

    public RemoveMarkerPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void prepareSuccessView(RemoveMarkerOutputData outputData) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setStopNames(outputData.getStopNames());
        state.setStops(outputData.getStops());
        state.setErrorMessage(null);
        searchViewModel.setState(state);
    }

    @Override
    public void prepareFailView(String error) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setErrorMessage(error);
        searchViewModel.setState(state);
    }
}
