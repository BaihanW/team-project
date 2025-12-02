package interface_adapter.suggestion;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import use_case.suggestion.SuggestionOutputBoundary;
import use_case.suggestion.SuggestionOutputData;

public class SuggestionPresenter implements SuggestionOutputBoundary {

    private final SearchViewModel searchViewModel;

    public SuggestionPresenter(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public void presentSuggestions(SuggestionOutputData data) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setSuggestions(data.getSuggestions());
        state.setSuggestionError(null);
        searchViewModel.setState(state);
        searchViewModel.firePropertyChange();
    }

    @Override
    public void presentError(String message) {
        SearchState state = new SearchState(searchViewModel.getState());
        state.setSuggestionError(message);
        searchViewModel.setState(state);
        searchViewModel.firePropertyChange();
    }
}