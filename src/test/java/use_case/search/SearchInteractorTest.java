package use_case.search;

import entity.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchInteractorTest {

    static class FakeDAO implements SearchDataAccessInterface {

        boolean existsReturn = true;
        Location loc = new Location("Toronto", 43.65, -79.38);

        @Override
        public boolean existsByName(String name) {
            return existsReturn;
        }

        @Override
        public Location get(String name) {
            return loc;
        }
    }

    static class FakePresenter implements SearchOutputBoundary {
        SearchOutputData success;
        String failure;

        @Override
        public void prepareSuccessView(SearchOutputData data) {
            success = data;
        }

        @Override
        public void prepareFailView(String error) {
            failure = error;
        }
    }

    @Test
    void successCase() {
        FakeDAO dao = new FakeDAO();
        FakePresenter presenter = new FakePresenter();

        SearchInteractor interactor = new SearchInteractor(dao, presenter);
        interactor.execute(new SearchInputData("Toronto"));

        assertNotNull(presenter.success);
        assertEquals("Toronto", presenter.success.getLocationName());
    }

}
