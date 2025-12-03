package interface_adapter.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.Test;
import use_case.remove_marker.RemoveMarkerInputBoundary;
import use_case.remove_marker.RemoveMarkerInputData;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RemoveMarkerControllerTest {

    @Test
    void controllerPassesInputDataToBoundary() {
        AtomicReference<RemoveMarkerInputData> captured = new AtomicReference<>();
        RemoveMarkerInputBoundary boundary = captured::set;
        RemoveMarkerController controller = new RemoveMarkerController(boundary);

        controller.removeAt(1, List.of("A"), List.of(new GeoPosition(0, 0)));

        RemoveMarkerInputData input = captured.get();
        assertNotNull(input);
        assertEquals(1, input.getIndex());
        assertEquals(List.of("A"), input.getStopNames());
        assertEquals(List.of(new GeoPosition(0, 0)), input.getStops());
    }
}