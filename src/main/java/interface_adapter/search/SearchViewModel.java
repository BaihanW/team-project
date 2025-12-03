package interface_adapter.search;

import interface_adapter.ViewModel;

public class SearchViewModel extends ViewModel<SearchState> {

    public SearchViewModel() {
        super("use_case/search");
        setState(new SearchState());
    }

    @Override
    public void showSaveSuccessMessage(String msg) {
        firePropertyChange("save_success", msg);
    }

    @Override
    public void showSaveErrorMessage(String msg) {
        firePropertyChange("save_error", msg);
    }
}
