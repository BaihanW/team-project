package use_case.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RemoveMarkerInteractorTest {

    @Test
    void invalidIndexTriggersFailureView() {
        RecordingPresenter presenter = new RecordingPresenter();
        RemoveMarkerInteractor interactor = new RemoveMarkerInteractor(presenter);

        interactor.execute(new RemoveMarkerInputData(-1, List.of(), List.of()));

        assertEquals("No marker selected to remove.", presenter.errorMessage);
        assertNull(presenter.outputData);
    }

    @Test
    void indexBeyondStopsTriggersFailureView() {
        RecordingPresenter presenter = new RecordingPresenter();
        RemoveMarkerInteractor interactor = new RemoveMarkerInteractor(presenter);

        interactor.execute(new RemoveMarkerInputData(2, List.of("One"), List.of(new GeoPosition(0, 0))));

        assertEquals("No marker selected to remove.", presenter.errorMessage);
        assertNull(presenter.outputData);
    }

    @Test
    void removesMarkerAndNotifiesPresenter() {
        List<String> names = new ArrayList<>(List.of("One", "Two", "Three"));
        List<GeoPosition> stops = new ArrayList<>(List.of(
                new GeoPosition(0, 0),
                new GeoPosition(1, 1),
                new GeoPosition(2, 2)
        ));
        RecordingPresenter presenter = new RecordingPresenter();
        RemoveMarkerInteractor interactor = new RemoveMarkerInteractor(presenter);

        interactor.execute(new RemoveMarkerInputData(1, names, stops));

        assertNotNull(presenter.outputData);
        assertEquals(1, presenter.outputData.getRemovedIndex());
        assertEquals(List.of("One", "Three"), presenter.outputData.getStopNames());
        assertEquals(List.of(new GeoPosition(0, 0), new GeoPosition(2, 2)), presenter.outputData.getStops());
        assertEquals(List.of("One", "Two", "Three"), names, "Input names list should remain unchanged");
        assertEquals(3, stops.size(), "Input stops list should remain unchanged");
    }

    @Test
    void handlesMissingStopNameEntry() {
        List<String> names = new ArrayList<>(List.of("One", "Two"));
        List<GeoPosition> stops = new ArrayList<>(List.of(
                new GeoPosition(0, 0),
                new GeoPosition(1, 1),
                new GeoPosition(2, 2)
        ));
        RecordingPresenter presenter = new RecordingPresenter();
        RemoveMarkerInteractor interactor = new RemoveMarkerInteractor(presenter);

        interactor.execute(new RemoveMarkerInputData(2, names, stops));

        assertNotNull(presenter.outputData);
        assertEquals(2, presenter.outputData.getRemovedIndex());
        assertEquals(names, presenter.outputData.getStopNames(), "Names list should be unchanged when index exceeds names size");
        assertEquals(List.of(new GeoPosition(0, 0), new GeoPosition(1, 1)), presenter.outputData.getStops());
    }

    private static class RecordingPresenter implements RemoveMarkerOutputBoundary {
        private RemoveMarkerOutputData outputData;
        private String errorMessage;

        @Override
        public void prepareSuccessView(RemoveMarkerOutputData outputData) {
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String error) {
            this.errorMessage = error;
        }
    }
}