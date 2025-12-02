package use_case.suggestion;

import entity.Location;
import use_case.search.SearchDataAccessInterface;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SuggestionInteractor implements SuggestionInputBoundary {
    private final SearchDataAccessInterface searchDataAccess;
    private final SuggestionOutputBoundary outputBoundary;

    public SuggestionInteractor(SearchDataAccessInterface searchDataAccess,
                                SuggestionOutputBoundary outputBoundary) {
        this.searchDataAccess = searchDataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(SuggestionInputData inputData) {
        final String rawQuery = inputData.getQuery();
        final String query = rawQuery == null ? "" : rawQuery.trim();

        if (query.isEmpty()) {
            outputBoundary.presentSuggestions(new SuggestionOutputData(Collections.emptyList()));
            return;
        }

        try {
            List<Location> matches = searchDataAccess.searchSuggestions(query, 5);
            List<String> suggestions = matches.stream()
                    .map(Location::getName)
                    .collect(Collectors.toList());
            outputBoundary.presentSuggestions(new SuggestionOutputData(suggestions));
        } catch (IOException e) {
            outputBoundary.presentError("Network error while fetching suggestions: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            outputBoundary.presentError("Request interrupted while fetching suggestions.");
        } catch (Exception e) {
            outputBoundary.presentError("Unexpected error: " + e.getMessage());
        }
    }
}