package app;

import interface_adapter.search.SearchController;
import interface_adapter.search.SearchPresenter;
import interface_adapter.search.SearchViewModel;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.remove_marker.RemoveMarkerPresenter;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;
import use_case.remove_marker.RemoveMarkerInputBoundary;
import use_case.remove_marker.RemoveMarkerInteractor;
import use_case.remove_marker.RemoveMarkerOutputBoundary;

import interface_adapter.addMarker.AddMarkerController;
import interface_adapter.addMarker.AddMarkerPresenter;
import interface_adapter.addMarker.AddMarkerViewModel;

import use_case.search.SearchDataAccessInterface;
import use_case.search.SearchInputBoundary;
import use_case.search.SearchInteractor;
import use_case.search.SearchOutputBoundary;

import use_case.add_marker.AddMarkerDataAccessInterface;
import use_case.add_marker.AddMarkerInputBoundary;
import use_case.add_marker.AddMarkerInteractor;
import use_case.add_marker.AddMarkerOutputBoundary;

import view.SearchView;

import javax.swing.*;
import java.awt.*;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import data_access.OSMDataAccessObject;
import entity.Location;
import entity.Marker;

public class AppBuilder {

    /* ============================================================
     * Fields
     * ============================================================ */

    private final JPanel cardPanel = new JPanel(new CardLayout());
    private JFrame appFrame;

    private SearchViewModel searchViewModel;
    private AddMarkerViewModel addMarkerViewModel;

    private SearchView searchView;

    private SearchController searchController;
    private AddMarkerController addMarkerController;

    // DAOs (인터페이스 의존)
    private final SearchDataAccessInterface searchDataAccess;
    private final AddMarkerDataAccessInterface addMarkerDataAccess;

    /* ============================================================
     * 0) 기본 생성자
     *    - Search: OSMDataAccessObject
     *    - AddMarker: 익명 in-memory DAO (MarkerDataAccessObject 파일 필요 없음)
     * ============================================================ */

    public AppBuilder() {
        this(
                new OSMDataAccessObject(HttpClient.newHttpClient()),
                new AddMarkerDataAccessInterface() {

                    // 메모리 안에만 마커들을 저장하는 간단한 구현
                    private final List<Marker> markers = new ArrayList<>();

                    @Override
                    public synchronized void save(Marker marker) {
                        markers.add(marker);
                    }

                    @Override
                    public synchronized boolean exists(Location location) {
                        // Location / Marker 둘 다 (lat, lon) 필드가 있다고 가정
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        for (Marker m : markers) {
                            if (Double.compare(m.getLatitude(), lat) == 0 &&
                                    Double.compare(m.getLongitude(), lon) == 0) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public synchronized List<Marker> allMarkers() {
                        return new ArrayList<>(markers);
                    }
                }
        );
    }

    /* ============================================================
     * DI 생성자 (테스트용 / 다른 구현 주입용)
     * ============================================================ */

    public AppBuilder(SearchDataAccessInterface searchDataAccess,
                      AddMarkerDataAccessInterface addMarkerDataAccess) {
        this.searchDataAccess = searchDataAccess;
        this.addMarkerDataAccess = addMarkerDataAccess;
    }

    /* ============================================================
     * 1) Search View 생성
     * ============================================================ */

    public AppBuilder addSearchView() {
        searchViewModel = new SearchViewModel();
        searchView = new SearchView(searchViewModel);
        cardPanel.add(searchView, searchView.getViewName());
        return this;
    }

    /* ============================================================
     * 2) Search Use Case 조립
     * ============================================================ */

    public AppBuilder addSearchUseCase() {
        SearchOutputBoundary searchPresenter =
                new SearchPresenter(searchViewModel);

        SearchInputBoundary searchInteractor =
                new SearchInteractor(searchDataAccess, searchPresenter);

        searchController = new SearchController(searchInteractor);
        searchView.setSearchController(searchController);

        return this;
    }

    public AppBuilder addRemoveMarkerUseCase() {
        final RemoveMarkerOutputBoundary removeMarkerOutputBoundary = new RemoveMarkerPresenter(searchViewModel);
        final RemoveMarkerInputBoundary removeMarkerInteractor = new RemoveMarkerInteractor(removeMarkerOutputBoundary);

        RemoveMarkerController removeMarkerController = new RemoveMarkerController(removeMarkerInteractor);
        searchView.setRemoveMarkerController(removeMarkerController);

        return this;
    }

    public JFrame build() {
        final JFrame application = new JFrame("trip planner");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    /* ============================================================
     * 2.5) AddMarker View (별도 뷰 없으면 no-op)
     * ============================================================ */

    public AppBuilder addAddMarkerView() {
        // AddMarker 전용 View 없고,
        // SearchView 내부 MapPanel 이 마커 뷰 역할 → 아무 것도 안 해도 됨.
        return this;
    }

    /* ============================================================
     * 3) AddMarker Use Case 조립
     * ============================================================ */

    public AppBuilder addAddMarkerUseCase() {
        addMarkerViewModel = new AddMarkerViewModel();

        AddMarkerOutputBoundary addMarkerPresenter =
                new AddMarkerPresenter(addMarkerViewModel);

        AddMarkerInputBoundary addMarkerInteractor =
                new AddMarkerInteractor(addMarkerDataAccess, addMarkerPresenter);

        addMarkerController = new AddMarkerController(addMarkerInteractor);

        // SearchView → MapPanel 으로 컨트롤러 전달
        searchView.setAddMarkerController(addMarkerController);

        return this;
    }

    /* ============================================================
     * 4) JFrame build
     * ============================================================ */

    public JFrame build() {
        if (appFrame == null) {
            appFrame = new JFrame("SwingTripPlanner");
            appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            appFrame.setContentPane(cardPanel);
            appFrame.pack();
            appFrame.setLocationRelativeTo(null);
        }
        return appFrame;
    }
}



