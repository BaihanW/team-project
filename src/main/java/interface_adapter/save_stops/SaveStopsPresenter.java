package interface_adapter.save_stops;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import use_case.save_stops.SaveStopsOutputBoundary;

public class SaveStopsPresenter implements SaveStopsOutputBoundary {

    private final SearchViewModel viewModel;

    public SaveStopsPresenter(SearchViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess() {
        SearchState state = new SearchState(viewModel.getState());
        state.setErrorMessage(null);
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }

    @Override
    public void presentFailure(String error) {
        SearchState state = new SearchState(viewModel.getState());
        state.setErrorMessage(error);
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}

