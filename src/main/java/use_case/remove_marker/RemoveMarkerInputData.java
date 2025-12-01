package use_case.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class RemoveMarkerInputData {
    private final int index;
    private final List<String> stopNames;
    private final List<GeoPosition> stops;

    public RemoveMarkerInputData(int index, List<String> stopNames, List<GeoPosition> stops) {
        this.index = index;
        this.stopNames = stopNames;
        this.stops = stops;
    }

    public int getIndex() {
        return index;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public List<GeoPosition> getStops() {
        return stops;
    }
}
