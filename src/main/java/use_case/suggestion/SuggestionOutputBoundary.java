package use_case.suggestion;

public interface SuggestionOutputBoundary {

    void presentSuggestions(SuggestionOutputData data);

    void presentError(String message);
}