package use_case.reorder;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class ReorderInteractor implements ReorderInputBoundary {
    private final ReorderOutputBoundary reorderPresenter;

    public ReorderInteractor(ReorderOutputBoundary reorderPresenter) {
        this.reorderPresenter = reorderPresenter;
    }

    @Override
    public void execute(ReorderInputData inputData) {
        int fromIndex = inputData.getFromIndex();
        int toIndex = inputData.getToIndex();
        List<String> names = new ArrayList<>(inputData.getStopNames());
        List<GeoPosition> stops = new ArrayList<>(inputData.getStops());
        int size = stops.size();
        if (fromIndex < 0 || fromIndex >= size || toIndex < 0 || toIndex >= size) {
            reorderPresenter.prepareFailView("Cannot move marker in that direction.");
            return;
        }

        GeoPosition movedStop = stops.remove(fromIndex);
        stops.add(toIndex, movedStop);
        String movedName = names.remove(fromIndex);
        names.add(toIndex, movedName);

        reorderPresenter.prepareSuccessView(new ReorderOutputData(fromIndex, toIndex, names, stops));
    }
}
