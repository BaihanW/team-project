package interface_adapter.addMarker;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInputData;

public class AddMarkerController {

    private final AddMarkerInputBoundary addMarkerInteractor;

    public AddMarkerController(AddMarkerInputBoundary addMarkerInteractor) {
        this.addMarkerInteractor = addMarkerInteractor;
    }

    /** for MapPanel */
    public void execute(double latitude, double longitude) {
        AddMarkerInputData inputData = new AddMarkerInputData(latitude, longitude);
        addMarkerInteractor.execute(inputData);
    }

    /**  lat, lon version */
    public void addMarker(double latitude, double longitude) {
        execute(latitude, longitude);
    }

    /**  addMarker(GeoPosition)  version covered just in case*/
    public void addMarker(GeoPosition pos) {
        if (pos != null) {
            execute(pos.getLatitude(), pos.getLongitude());
        }
    }
}

