package interface_adapter.suggestion;

import use_case.suggestion.SuggestionInputBoundary;
import use_case.suggestion.SuggestionInputData;

public class SuggestionController {

    private final SuggestionInputBoundary suggestionInteractor;

    public SuggestionController(SuggestionInputBoundary suggestionInteractor) {
        this.suggestionInteractor = suggestionInteractor;
    }

    public void execute(String query) {
        suggestionInteractor.execute(new SuggestionInputData(query));
    }
}