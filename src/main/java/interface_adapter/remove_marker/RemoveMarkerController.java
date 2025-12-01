package interface_adapter.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.remove_marker.RemoveMarkerInputBoundary;
import use_case.remove_marker.RemoveMarkerInputData;

import java.util.List;

/**
 * remove marker usecase controller.
 */

public class RemoveMarkerController {
    private final RemoveMarkerInputBoundary removeMarkerInputBoundary;

    public RemoveMarkerController(RemoveMarkerInputBoundary removeMarkerInputBoundary) {
        this.removeMarkerInputBoundary = removeMarkerInputBoundary;
    }

    public void removeAt(int index, List<String> stopNames, List<GeoPosition> stops) {
        removeMarkerInputBoundary.execute(new RemoveMarkerInputData(index, stopNames, stops));
    }
}
