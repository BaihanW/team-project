package app;

import data_access.FileStopListDAO;
import data_access.OSMDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.save_stops.SaveStopsPresenter;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchViewModel;
import use_case.save_stops.SaveStopsInputBoundary;
import use_case.save_stops.SaveStopsInteractor;
import use_case.save_stops.SaveStopsOutputBoundary;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;
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

    private final String stopListPath = "src/main/";
    final FileStopListDAO fileStopListDAO = new FileStopListDAO(stopListPath);

    private SearchViewModel searchViewModel;
    private SearchView searchView;

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
