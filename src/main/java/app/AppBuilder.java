package app;

import data_access.FileStopListDAO;
import data_access.OSMDataAccessObject;
import data_access.RoutingDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.generate_route.GenerateRoutePresenter;
import interface_adapter.generate_route.GenerateRouteViewModel;
import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.save_stops.SaveStopsPresenter;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchViewModel;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.remove_marker.RemoveMarkerPresenter;
import use_case.generate_route.GenerateRouteInputBoundary;
import use_case.generate_route.GenerateRouteInteractor;
import use_case.generate_route.GenerateRouteOutputBoundary;
import use_case.save_stops.SaveStopsInputBoundary;
import use_case.save_stops.SaveStopsInteractor;
import use_case.save_stops.SaveStopsOutputBoundary;
import interface_adapter.suggestion.SuggestionController;
import interface_adapter.suggestion.SuggestionPresenter;
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

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    public AppBuilder addSearchView() {
        searchViewModel = new SearchViewModel();
        generateRouteViewModel = new GenerateRouteViewModel();
        searchView = new SearchView(searchViewModel, generateRouteViewModel);
        cardPanel.add(searchView, searchView.getViewName());
        return this;
    }

    public AppBuilder addGenerateRouteUseCase() {
        final GenerateRouteOutputBoundary generateRoutePresenter = new GenerateRoutePresenter(generateRouteViewModel);
        final GenerateRouteInputBoundary generateRouteInteractor = new GenerateRouteInteractor(
                routingDataAccessObject, generateRoutePresenter);

        GenerateRouteController generateRouteController = new GenerateRouteController(generateRouteInteractor);
        searchView.setGenerateRouteController(generateRouteController);

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
        final RemoveMarkerInputBoundary removeMarkerInteractor = new RemoveMarkerInteractor(removeMarkerOutputBoundary);

        RemoveMarkerController removeMarkerController = new RemoveMarkerController(removeMarkerInteractor);
        searchView.setRemoveMarkerController(removeMarkerController);

        return this;
    }

    public AppBuilder loadStopsOnStartup() {
        try {
            FileStopListDAO.LoadedStops stored = fileStopListDAO.load();

            if (!stored.names.isEmpty()) {

                var state = searchViewModel.getState();

                // Load stops into state
                state.setStopNames(stored.names);
                state.setStops(stored.positions);

                // Center map on the last stop
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



    public JFrame build() {
        final JFrame application = new JFrame("trip planner");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        application.add(cardPanel);

        viewManagerModel.setState(searchView.getViewName());
        viewManagerModel.firePropertyChange();
        loadStopsOnStartup();

        return application;
    }
}
