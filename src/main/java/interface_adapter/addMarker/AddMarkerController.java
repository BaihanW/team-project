package interface_adapter.addMarker;

import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInputData;

public class AddMarkerController {
    private final AddMarkerInputBoundary interactor;

    public AddMarkerController(AddMarkerInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(double lat, double lon) {
        AddMarkerInputData data = new AddMarkerInputData(lat, lon);
        interactor.execute(data);
    }
}