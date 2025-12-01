package use_case.reorder;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public interface ReorderDataAccessInterface {

    List<String> getStopNames();

    List<GeoPosition> getStops();
}
