package use_case.save_stops;

import org.jxmapviewer.viewer.GeoPosition;

import java.io.IOException;
import java.util.List;

public interface SaveStopsDataAccessInterface {
    void save(List<String> names, List<GeoPosition> positions) throws IOException;
}

