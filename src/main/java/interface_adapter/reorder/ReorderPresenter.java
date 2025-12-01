package interface_adapter.reorder;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import use_case.reorder.ReorderOutputBoundary;
import use_case.reorder.ReorderOutputData;

public class ReorderPresenter implements ReorderOutputBoundary {

    private final SearchViewModel searchViewModel;

    public ReorderPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void prepareSuccessView(ReorderOutputData outputData) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setStopNames(outputData.getStopNames());
        state.setStops(outputData.getStops());
        state.setErrorMessage(null);
        searchViewModel.setState(state);
        searchViewModel.firePropertyChange("stops");
    }

    @Override
    public void prepareFailView(String error) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setErrorMessage(error);
        searchViewModel.setState(state);
        searchViewModel.firePropertyChange("error");
    }
}
