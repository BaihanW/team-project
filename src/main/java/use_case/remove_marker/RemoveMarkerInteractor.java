package use_case.remove_marker;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class RemoveMarkerInteractor implements RemoveMarkerInputBoundary {
    private final RemoveMarkerOutputBoundary removeMarkerPresenter;

    public RemoveMarkerInteractor(RemoveMarkerOutputBoundary removeMarkerPresenter) {
        this.removeMarkerPresenter = removeMarkerPresenter;
    }

    @Override
    public void execute(RemoveMarkerInputData inputData) {
        int index = inputData.getIndex();
        List<GeoPosition> stops = new ArrayList<>(inputData.getStops());
        List<String> names = new ArrayList<>(inputData.getStopNames());

        if (index < 0 || index >= stops.size()) {
            removeMarkerPresenter.prepareFailView("No marker selected to remove.");
            return;
        }

        stops.remove(index);
        if (index < names.size()) {
            names.remove(index);
        }

        removeMarkerPresenter.prepareSuccessView(new RemoveMarkerOutputData(index, names, stops));
    }
}
