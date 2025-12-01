package use_case.save_stops;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class SaveStopsInputData {
    private final List<String> names;
    private final List<GeoPosition> positions;

    public SaveStopsInputData(List<String> names, List<GeoPosition> positions) {
        this.names = names;
        this.positions = positions;
    }

    public List<String> getNames() { return names; }
    public List<GeoPosition> getPositions() { return positions; }
}