package app;

import data_access.FileStopListDAO;
import data_access.OSMDataAccessObject;
import data_access.RoutingDataAccessObject;
import interface_adapter.ViewManagerModel;

import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.generate_route.GenerateRoutePresenter;
import interface_adapter.generate_route.GenerateRouteViewModel;

import interface_adapter.reorder.ReorderController;
import interface_adapter.reorder.ReorderPresenter;

import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.save_stops.SaveStopsPresenter;

import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;

import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.remove_marker.RemoveMarkerPresenter;

import interface_adapter.suggestion.SuggestionController;
import interface_adapter.suggestion.SuggestionPresenter;

import use_case.generate_route.GenerateRouteInputBoundary;
import use_case.generate_route.GenerateRouteInteractor;
import use_case.generate_route.GenerateRouteOutputBoundary;

import use_case.reorder.ReorderInputBoundary;
import use_case.reorder.ReorderInteractor;
import use_case.reorder.ReorderOutputBoundary;

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

import view.SearchView;
import view.ViewManager;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;

/**
 * Configures and wires the application using the simplified Clean Architecture graph.
 */
public class AppBuilder {

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private final HttpClient client = HttpClient.newHttpClient();
    final OSMDataAccessObject osmDataAccessObject = new OSMDataAccessObject(client);
    final RoutingDataAccessObject routingDataAccessObject = new RoutingDataAccessObject(client);

    private final String stopListPath = "src/main/";
    final FileStopListDAO fileStopListDAO = new FileStopListDAO(stopListPath);

    private SearchViewModel searchViewModel;
    private GenerateRouteViewModel generateRouteViewModel;
    private SearchView searchView;

    final InMemoryMarkerDataAccessObject markerDataAccessObject = new InMemoryMarkerDataAccessObject();

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    /** ---------------------------
     *  VIEW 생성
     * --------------------------- */
    public AppBuilder addSearchView() {
        searchViewModel = new SearchViewModel();
        generateRouteViewModel = new GenerateRouteViewModel();

        searchView = new SearchView(searchViewModel, generateRouteViewModel);
        cardPanel.add(searchView, searchView.getViewName());
        return this;
    }

    /** ---------------------------
     *  ROUTE USE CASE
     * --------------------------- */
    public AppBuilder addGenerateRouteUseCase() {
        GenerateRouteOutputBoundary presenter = new GenerateRoutePresenter(generateRouteViewModel);
        GenerateRouteInputBoundary interactor =
                new GenerateRouteInteractor(routingDataAccessObject, presenter);
        GenerateRouteController controller = new GenerateRouteController(interactor);
        searchView.setGenerateRouteController(controller);
        return this;
    }

    /** ---------------------------
     *  SEARCH USE CASE
     * --------------------------- */
    public AppBuilder addSearchUseCase() {
        SearchOutputBoundary presenter = new SearchPresenter(searchViewModel);
        SearchInputBoundary interactor =
                new SearchInteractor(osmDataAccessObject, presenter);
        SearchController controller = new SearchController(interactor);
        searchView.setSearchController(controller);
        return this;
    }

    /** ---------------------------
     *  SAVE USE CASE
     * --------------------------- */
    public AppBuilder addSaveStopsUseCase() {
        SaveStopsOutputBoundary presenter = new SaveStopsPresenter(searchViewModel);
        SaveStopsInputBoundary interactor =
                new SaveStopsInteractor(fileStopListDAO, presenter);
        SaveStopsController controller = new SaveStopsController(interactor);
        searchView.setSaveStopsController(controller);
        return this;
    }

    /** ---------------------------
     *  SUGGESTION USE CASE
     * --------------------------- */
    public AppBuilder addSuggestionUseCase() {
        SuggestionOutputBoundary presenter = new SuggestionPresenter(searchViewModel);
        SuggestionInputBoundary interactor =
                new SuggestionInteractor(osmDataAccessObject, presenter);
        SuggestionController controller = new SuggestionController(interactor);
        searchView.setSuggestionController(controller);
        return this;
    }

    /** ---------------------------
     *  REMOVE MARKER USE CASE
     * --------------------------- */
    public AppBuilder addRemoveMarkerUseCase() {
        RemoveMarkerOutputBoundary presenter = new RemoveMarkerPresenter(searchViewModel);
        RemoveMarkerInputBoundary interactor =
                new RemoveMarkerInteractor(presenter);
        RemoveMarkerController controller = new RemoveMarkerController(interactor);
        searchView.setRemoveMarkerController(controller);
        return this;
    }

    /** ---------------------------
     *  REORDER USE CASE
     * --------------------------- */
    public AppBuilder addReorderUseCase() {
        ReorderOutputBoundary presenter = new ReorderPresenter(searchViewModel);
        ReorderInputBoundary interactor =
                new ReorderInteractor(presenter);
        ReorderController controller = new ReorderController(interactor);
        searchView.setReorderController(controller);
        return this;
    }

    /** ---------------------------
     *  STARTUP STOP LOADING
     * --------------------------- */
    public AppBuilder loadStopsOnStartup() {
        try {
            FileStopListDAO.LoadedStops stored = fileStopListDAO.load();

            if (!stored.names.isEmpty()) {

                var state = searchViewModel.getState();

                state.setStopNames(stored.names);
                state.setStops(stored.positions);

                var last = stored.positions.get(stored.positions.size() - 1);
                state.setLatitude(last.getLatitude());
                state.setLongitude(last.getLongitude());

                searchViewModel.setState(state);
                searchViewModel.firePropertyChange();
            }

        } catch (Exception e) {
            System.err.println("Failed to load saved stops: " + e.getMessage());
        }

        return this;
    }

    /** ---------------------------
     *  BUILD
     * --------------------------- */
    public JFrame build() {
        JFrame app = new JFrame("trip planner");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        app.add(cardPanel);

        viewManagerModel.setState(searchView.getViewName());
        viewManagerModel.firePropertyChange();

        loadStopsOnStartup();

        return app;
    }
}
