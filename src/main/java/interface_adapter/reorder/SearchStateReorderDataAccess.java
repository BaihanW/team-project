package interface_adapter.reorder;

import interface_adapter.search.SearchViewModel;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.reorder.ReorderDataAccessInterface;

import java.util.ArrayList;
import java.util.List;

public class SearchStateReorderDataAccess implements ReorderDataAccessInterface {

    private final SearchViewModel searchViewModel;

    public SearchStateReorderDataAccess(SearchViewModel searchViewModel) {
        this.searchViewModel = searchViewModel;
    }

    @Override
    public List<String> getStopNames() {
        return new ArrayList<>(searchViewModel.getState().getStopNames());
    }

    @Override
    public List<GeoPosition> getStops() {
        return new ArrayList<>(searchViewModel.getState().getStops());
    }
}
