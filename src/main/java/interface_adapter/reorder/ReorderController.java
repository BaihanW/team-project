package interface_adapter.reorder;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.reorder.ReorderInputBoundary;
import use_case.reorder.ReorderInputData;

import java.util.List;

public class ReorderController {
    private final ReorderInputBoundary reorderInputBoundary;

    public ReorderController(ReorderInputBoundary reorderInputBoundary) {
        this.reorderInputBoundary = reorderInputBoundary;
    }

    public void move(int fromIndex, int toIndex, List<String> stopNames, List<GeoPosition> stops) {
        reorderInputBoundary.execute(new ReorderInputData(fromIndex, toIndex, stopNames, stops));
    }
}
