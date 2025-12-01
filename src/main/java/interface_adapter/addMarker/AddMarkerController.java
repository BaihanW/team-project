package interface_adapter.addMarker;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInputData;

public class AddMarkerController {

    private final AddMarkerInputBoundary addMarkerInteractor;

    public AddMarkerController(AddMarkerInputBoundary addMarkerInteractor) {
        this.addMarkerInteractor = addMarkerInteractor;
    }

    /** MapPanel에서 쓰는 버전 */
    public void execute(double latitude, double longitude) {
        AddMarkerInputData inputData = new AddMarkerInputData(latitude, longitude);
        addMarkerInteractor.execute(inputData);
    }

    /** 🔹다른 코드가 addMarker(lat, lon) 를 호출하는 경우 호환용 */
    public void addMarker(double latitude, double longitude) {
        execute(latitude, longitude);
    }

    /** 🔹혹시 addMarker(GeoPosition) 으로 부르는 코드가 있으면 이것도 커버 */
    public void addMarker(GeoPosition pos) {
        if (pos != null) {
            execute(pos.getLatitude(), pos.getLongitude());
        }
    }
}

