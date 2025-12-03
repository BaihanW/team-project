package interface_adapter.remove_marker;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchStateRemoveDataAccessTest {

    @Test
    void returnsCopiesOfStopsAndNames() {
        SearchState state = new SearchState();
        state.setStopNames(List.of("First"));
        state.setStops(List.of(new GeoPosition(1, 2)));

        SearchViewModel viewModel = new SearchViewModel();
        viewModel.setState(state);

        SearchStateRemoveDataAccess dataAccess = new SearchStateRemoveDataAccess(viewModel);
        List<String> names = dataAccess.getStopNames();
        List<GeoPosition> stops = dataAccess.getStops();

        assertEquals(List.of("First"), names);
        assertEquals(List.of(new GeoPosition(1, 2)), stops);

        names.add("New");
        stops.add(new GeoPosition(3, 4));

        assertEquals(1, viewModel.getState().getStopNames().size(), "Original state should not change when copies are mutated");
        assertEquals(1, viewModel.getState().getStops().size(), "Original stop list should not change when copies are mutated");
    }
}