package app;

import data_access.OSMDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchViewModel;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;
import view.SearchView;
import view.ViewManager;
import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;

/**
 * Builder of the final GUI
 */
public class AppBuilder {

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private final HttpClient client = HttpClient.newHttpClient();
    final OSMDataAccessObject osmDataAccessObject = new OSMDataAccessObject(client);


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

    public JFrame build() {
        final JFrame application = new JFrame("trip planner");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        application.add(cardPanel);

        viewManagerModel.setState(searchView.getViewName());
        viewManagerModel.firePropertyChange();

        return application;
    }


}
