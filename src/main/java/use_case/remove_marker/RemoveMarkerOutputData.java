package use_case.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class RemoveMarkerOutputData {
    private final int removedIndex;
    private final List<String> stopNames;
    private final List<GeoPosition> stops;

    public RemoveMarkerOutputData(int removedIndex, List<String> stopNames, List<GeoPosition> stops) {
        this.removedIndex = removedIndex;
        this.stopNames = stopNames;
        this.stops = stops;
    }

    public int getRemovedIndex() {
        return removedIndex;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public List<GeoPosition> getStops() {
        return stops;
    }
}
