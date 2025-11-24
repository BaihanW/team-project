package interface_adapter.addMarker;

import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInputData;

/**
 * Controller for the Add Marker use case
 *
 */
public class AddMarkerController {
    private final AddMarkerInputBoundary addMarkerInputBoundary;

    public AddMarkerController(AddMarkerInputBoundary addMarkerInputBoundary) {
        this.addMarkerInputBoundary = addMarkerInputBoundary;
    }

    public void addMarker(double latitude, double longitude) {
        AddMarkerInputData inputData = new AddMarkerInputData(latitude, longitude);
        addMarkerInputBoundary.execute(inputData);
    }
}
