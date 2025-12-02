package use_case.suggestion;

public class SuggestionInputData {
    private final String query;

    public SuggestionInputData(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}