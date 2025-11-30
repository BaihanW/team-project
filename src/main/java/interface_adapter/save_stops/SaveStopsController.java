package interface_adapter.save_stops;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.save_stops.SaveStopsInputBoundary;
import use_case.save_stops.SaveStopsInputData;
import java.util.List;

public class SaveStopsController {

    private final SaveStopsInputBoundary interactor;

    public SaveStopsController(SaveStopsInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(List<String> names, List<GeoPosition> positions) {
        SaveStopsInputData data = new SaveStopsInputData(names, positions);
        interactor.execute(data);
    }
}

