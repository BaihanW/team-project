package use_case.reorder;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class ReorderInputData {
    private final int fromIndex;
    private final int toIndex;
    private final List<String> stopNames;
    private final List<GeoPosition> stops;

    public ReorderInputData(int fromIndex, int toIndex, List<String> stopNames, List<GeoPosition> stops) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.stopNames = stopNames;
        this.stops = stops;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public List<GeoPosition> getStops() {
        return stops;
    }
}
