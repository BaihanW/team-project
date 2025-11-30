package interface_adapter.search;

import org.junit.jupiter.api.Test;
import use_case.search.SearchOutputData;

import static org.junit.jupiter.api.Assertions.*;

class SearchPresenterTest {

    @Test
    void presentSuccessUpdatesViewModel() {
        SearchViewModel vm = new SearchViewModel();
        SearchPresenter presenter = new SearchPresenter(vm);

        SearchOutputData out = new SearchOutputData(
                "Toronto", 43.65, -79.38
        );

        presenter.prepareSuccessView(out);

        SearchState s = vm.getState();
        assertEquals("Toronto", s.getLocationName());
        assertEquals(43.65, s.getLatitude());
        assertEquals(-79.38, s.getLongitude());
        assertNull(s.getSearchError());
    }

    @Test
    void presentFailureSetsError() {
        SearchViewModel vm = new SearchViewModel();
        SearchPresenter presenter = new SearchPresenter(vm);

        presenter.prepareFailView("Invalid location");

        assertEquals("Invalid location", vm.getState().getSearchError());
    }
}
