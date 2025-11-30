package use_case.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

/**
 * Gateway that exposes the current itinerary stops for the remove-marker use case.
 */
public interface RemoveMarkerDataAccessInterface {
    List<String> getStopNames();
    List<GeoPosition> getStops();
}
