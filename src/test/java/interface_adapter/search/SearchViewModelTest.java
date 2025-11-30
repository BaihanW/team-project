package interface_adapter.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchViewModelTest {

    @Test
    void testInitialState() {
        SearchViewModel vm = new SearchViewModel();
        assertNotNull(vm.getState());
    }

    @Test
    void testSetStateUpdates() {
        SearchViewModel vm = new SearchViewModel();

        SearchState state = new SearchState();
        state.setLocationName("Toronto");
        state.setLatitude(43.65);
        state.setLongitude(-79.38);

        vm.setState(state);

        assertEquals("Toronto", vm.getState().getLocationName());
        assertEquals(43.65, vm.getState().getLatitude());
        assertEquals(-79.38, vm.getState().getLongitude());
    }
}
