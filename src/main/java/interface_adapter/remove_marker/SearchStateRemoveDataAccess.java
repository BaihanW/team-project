package interface_adapter.remove_marker;

import interface_adapter.search.SearchViewModel;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.remove_marker.RemoveMarkerDataAccessInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter exposing stops from {@link SearchViewModel} to the remove-marker interactor.
 */
public class SearchStateRemoveDataAccess implements RemoveMarkerDataAccessInterface {

    private final SearchViewModel searchViewModel;

    public SearchStateRemoveDataAccess(SearchViewModel searchViewModel) {
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
