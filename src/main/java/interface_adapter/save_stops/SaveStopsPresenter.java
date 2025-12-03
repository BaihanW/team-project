package interface_adapter.save_stops;

import interface_adapter.search.SearchViewModel;
import use_case.save_stops.SaveStopsOutputBoundary;

public class SaveStopsPresenter implements SaveStopsOutputBoundary {

    private final SearchViewModel viewModel;

    public SaveStopsPresenter(SearchViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess() {
        viewModel.showSaveSuccessMessage("Stops saved!");
    }

    @Override
    public void presentFailure(String error) {
        viewModel.showSaveErrorMessage(error);
    }
}

