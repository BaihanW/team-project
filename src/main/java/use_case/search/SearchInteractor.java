package use_case.search;

import entity.Location;

import java.io.IOException;

/**
 * The Search Interactor.
 */
public class SearchInteractor implements SearchInputBoundary {
    private final SearchDataAccessInterface searchDataAccessObj;
    private final SearchOutputBoundary searchPresenter;

    public SearchInteractor(SearchDataAccessInterface userDataAccessInterface,
                            SearchOutputBoundary searchOutputBoundary) {
        this.searchDataAccessObj = userDataAccessInterface;
        this.searchPresenter = searchOutputBoundary;
    }

    @Override
    public void execute(SearchInputData searchInputData) {
        final String locationName = searchInputData.getLocationName();
        try {
            if (!searchDataAccessObj.existsByName(locationName)) {
                searchPresenter.prepareFailView(locationName + ": Location does not exist.");
            }
            else {
                // get location data from OSM
                final Location location = searchDataAccessObj.get(searchInputData.getLocationName());

                final SearchOutputData searchOutputData = new SearchOutputData(location.getName(),
                        location.getLatitude(), location.getLongitude());
                searchPresenter.prepareSuccessView(searchOutputData);
            }
        } catch (IOException e) {
            searchPresenter.prepareFailView("Network error while searching for location: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            searchPresenter.prepareFailView("Request interrupted while searching for location.");
        } catch (Exception e) {
            searchPresenter.prepareFailView("Unexpected error: " + e.getMessage());
        }
    }
}
