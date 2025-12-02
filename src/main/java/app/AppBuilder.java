package app;

import data_access.FileStopListDAO;
import data_access.OSMDataAccessObject;
import interface_adapter.ViewManagerModel;

import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.save_stops.SaveStopsPresenter;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.remove_marker.RemoveMarkerPresenter;
import interface_adapter.suggestion.SuggestionController;
import interface_adapter.suggestion.SuggestionPresenter;

import interface_adapter.addMarker.AddMarkerController;
import interface_adapter.addMarker.AddMarkerPresenter;
import interface_adapter.addMarker.AddMarkerViewModel;

import use_case.save_stops.SaveStopsInputBoundary;
import use_case.save_stops.SaveStopsInteractor;
import use_case.save_stops.SaveStopsOutputBoundary;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;
import use_case.remove_marker.RemoveMarkerInputBoundary;
import use_case.remove_marker.RemoveMarkerInteractor;
import use_case.remove_marker.RemoveMarkerOutputBoundary;
import use_case.suggestion.SuggestionInputBoundary;
import use_case.suggestion.SuggestionInteractor;
import use_case.suggestion.SuggestionOutputBoundary;

import use_case.add_marker.AddMarkerDataAccessInterface;
import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInteractor;
import use_case.add_marker.AddMarkerOutputBoundary;

import entity.Marker;
import entity.Location;

import view.SearchView;
import view.ViewManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configures and wires the application using the simplified Clean Architecture graph
 */
public class AppBuilder {

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private final HttpClient client = HttpClient.newHttpClient();
    final OSMDataAccessObject osmDataAccessObject = new OSMDataAccessObject(client);

    private final String stopListPath = "src/main/";
    final FileStopListDAO fileStopListDAO = new FileStopListDAO(stopListPath);

    private SearchViewModel searchViewModel;
    private SearchView searchView;

    private AddMarkerViewModel addMarkerViewModel;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    public AppBuilder addSearchView() {
        searchViewModel = new SearchViewModel();
        searchView = new SearchView(searchViewModel);
        cardPanel.add(searchView, searchView.getViewName());
        return this;
    }

    public AppBuilder addSearchUseCase() {
        final SearchOutputBoundary searchOutputBoundary = new SearchPresenter(searchViewModel);
        final SearchInputBoundary searchInteractor = new SearchInteractor(
                osmDataAccessObject, searchOutputBoundary);

        SearchController searchController = new SearchController(searchInteractor);
        searchView.setSearchController(searchController);

        return this;
    }

    public AppBuilder addSaveStopsUseCase() {
        final SaveStopsOutputBoundary saveStopsOutputBoundary = new SaveStopsPresenter(searchViewModel);
        final SaveStopsInputBoundary saveStopsInteractor = new SaveStopsInteractor(
                fileStopListDAO, saveStopsOutputBoundary);

        SaveStopsController saveStopsController = new SaveStopsController(saveStopsInteractor);
        searchView.setSaveStopsController(saveStopsController);

        return this;
    }

    public AppBuilder addSuggestionUseCase() {
        final SuggestionOutputBoundary outputBoundary = new SuggestionPresenter(searchViewModel);
        final SuggestionInputBoundary interactor = new SuggestionInteractor(osmDataAccessObject, outputBoundary);

        SuggestionController suggestionController = new SuggestionController(interactor);
        searchView.setSuggestionController(suggestionController);

        return this;
    }

    public AppBuilder addRemoveMarkerUseCase() {
        final RemoveMarkerOutputBoundary removeMarkerOutputBoundary = new RemoveMarkerPresenter(searchViewModel);
        RemoveMarkerInteractor removeMarkerInteractor = new RemoveMarkerInteractor(removeMarkerOutputBoundary);

        RemoveMarkerController removeMarkerController = new RemoveMarkerController(removeMarkerInteractor);
        searchView.setRemoveMarkerController(removeMarkerController);

        return this;
    }

    public AppBuilder addAddMarkerView() {
        addMarkerViewModel = new AddMarkerViewModel();
        return this;
    }

    public AppBuilder addAddMarkerUseCase() {
        if (addMarkerViewModel == null) {
            addAddMarkerView();
        }

        AddMarkerDataAccessInterface addMarkerDataAccess = new AddMarkerDataAccessInterface() {
            private final List<Marker> markers = new ArrayList<>();

            @Override
            public void save(Marker marker) {
                markers.add(marker);
            }

            @Override
            public boolean exist(Location location) {
                for (Marker m : markers) {
                    Location loc = m.getLocation();
                    if (Double.compare(loc.getLatitude(), location.getLatitude()) == 0 &&
                            Double.compare(loc.getLongitude(), location.getLongitude()) == 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public List<Marker> allMarkers() {
                return Collections.unmodifiableList(markers);
            }
        };

        AddMarkerOutputBoundary addMarkerOutputBoundary = new AddMarkerPresenter(addMarkerViewModel);
        AddMarkerInputBoundary addMarkerInteractor =
                new AddMarkerInteractor(addMarkerDataAccess, addMarkerOutputBoundary);
        AddMarkerController addMarkerController = new AddMarkerController(addMarkerInteractor);

        return this;
    }

    public AppBuilder loadStopsOnStartup() {
        try {
            FileStopListDAO.LoadedStops stored = fileStopListDAO.load();

            if (!stored.names().isEmpty()) {

                SearchState state = new SearchState(searchViewModel.getState());

                state.setStopNames(stored.names());
                state.setStops(stored.positions());

                if (!stored.positions().isEmpty()) {
                    var firstStop = stored.positions().get(0);
                    state.setLatitude(firstStop.getLatitude());
                    state.setLongitude(firstStop.getLongitude());
                    state.setLocationName(stored.names().get(0));
                }

                searchViewModel.setState(state);
                searchViewModel.firePropertyChange();

                searchView.showMarkersForCurrentStops();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public JFrame build() {
        final JFrame application = new JFrame("trip planner");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        application.add(cardPanel);

        viewManagerModel.setState(searchView.getViewName());
        viewManagerModel.firePropertyChange();

        return application;
    }
}
