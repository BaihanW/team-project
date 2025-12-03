package interface_adapter.remove_marker;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.remove_marker.RemoveMarkerOutputData;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RemoveMarkerPresenterTest {

    private SearchViewModel viewModel;
    private AtomicReference<PropertyChangeEvent> lastEvent;

    @BeforeEach
    void setUp() {
        viewModel = new SearchViewModel();
        lastEvent = new AtomicReference<>();
        viewModel.addPropertyChangeListener(lastEvent::set);
    }

    @Test
    void prepareSuccessViewUpdatesStopsAndClearsErrors() {
        SearchState initial = viewModel.getState();
        initial.setErrorMessage("old error");
        viewModel.setState(initial);

        RemoveMarkerPresenter presenter = new RemoveMarkerPresenter(viewModel);
        presenter.prepareSuccessView(new RemoveMarkerOutputData(0,
                List.of("A", "B"),
                List.of(new GeoPosition(1, 1))));

        SearchState updated = viewModel.getState();
        assertEquals(List.of("A", "B"), updated.getStopNames());
        assertEquals(List.of(new GeoPosition(1, 1)), updated.getStops());
        assertNull(updated.getErrorMessage());
        assertNotNull(lastEvent.get());
    }

    @Test
    void prepareFailViewSetsErrorMessage() {
        RemoveMarkerPresenter presenter = new RemoveMarkerPresenter(viewModel);
        presenter.prepareFailView("unable to remove");

        SearchState updated = viewModel.getState();
        assertEquals("unable to remove", updated.getErrorMessage());
        assertNotNull(lastEvent.get());
    }
}