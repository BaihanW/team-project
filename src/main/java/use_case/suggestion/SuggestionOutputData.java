package use_case.suggestion;

import java.util.List;

public class SuggestionOutputData {
    private final List<String> suggestions;

    public SuggestionOutputData(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}