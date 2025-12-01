package view;

import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.addMarker.AddMarkerController;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

/**
 * SearchView
 *  - SearchBar + StopsList + MapPanel
 *  - SearchViewModel 상태 관찰
 *  - SearchController / AddMarkerController 는 AppBuilder에서 주입
 */
public class SearchView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName;
    private final transient SearchViewModel searchViewModel;

    private final JTextField searchInputField = new JTextField(15);
    private final JButton searchButton = new JButton("Search");
    private final JButton routeButton = new JButton("Route");
    private final JButton moveUpButton = new JButton("Up");
    private final JButton moveDownButton = new JButton("Down");
    private final JButton removeButton = new JButton("Remove");

    private transient SearchController searchController = null;
    private transient RemoveMarkerController removeMarkerController = null;

    // 🔹 MapPanel은 여기서 생성 (Clean Architecture: View가 View를 소유)
    private final MapPanel mapPanel = new MapPanel();

    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);

    public SearchView(SearchViewModel searchViewModel) {
        this.viewName = searchViewModel.getViewName();
        this.searchViewModel = searchViewModel;

        this.searchViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JSplitPane layoutSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftSidebar(),
                buildRightMapPanel()
        );
        layoutSplit.setDividerLocation(350);
        layoutSplit.setOneTouchExpandable(true);

        add(layoutSplit, BorderLayout.CENTER);
        attachGlobalEnterKey();
        attachStopsListDoubleClickListener();
        attachSearchFieldListener();
        attachSearchButtonListener();
        attachRemoveButtonListener();
    }

    /* ===== controller setters ===== */

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    /** AppBuilder에서 AddMarkerController 주입 → 내부 MapPanel에 넘김 */
    public void setAddMarkerController(AddMarkerController addMarkerController) {
        this.mapPanel.setAddMarkerController(addMarkerController);
    }

    /* ------------------------------------------------------------------ */

    private JPanel buildLeftSidebar() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(350, 800));
        left.setBackground(new Color(70, 130, 180));

        left.add(buildSearchSection(), BorderLayout.NORTH);
        left.add(buildStopsListSection(), BorderLayout.CENTER);
        left.add(buildStopsControlSection(), BorderLayout.SOUTH);

        return left;
    }

    private JPanel buildSearchSection() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(routeButton);

        container.add(searchInputField, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.EAST);

        return container;
    }

    private JScrollPane buildStopsListSection() {
        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stopsList.setBackground(new Color(220, 235, 245));
        stopsList.setCellRenderer(new StopCellRenderer(stopsListModel));

        JScrollPane scroll = new JScrollPane(stopsList);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scroll;
    }

    private JPanel buildStopsControlSection() {
        JPanel controls = new JPanel(new GridLayout(1, 3, 5, 5));
        controls.setOpaque(false);

        controls.add(moveUpButton);     // Placeholder for clean architecture hooks
        controls.add(moveDownButton);
        controls.add(removeButton);
        controls.add(new JButton("Up"));
        controls.add(new JButton("Down"));
        controls.add(new JButton("Remove"));

        return controls;
    }

    private JPanel buildRightMapPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.add(mapPanel, BorderLayout.CENTER);
        return right;
    }

    /* ===== 이벤트 연결 ===== */

    private void attachSearchButtonListener() {
        searchButton.addActionListener(evt -> {
            if (searchController == null) return;
            String text = searchViewModel.getState().getLocationName();
            searchController.execute(text);
        });
    }

    private void attachRemoveButtonListener() {
        removeButton.addActionListener(evt -> {
            if (removeMarkerController == null) return;

            int selectedIndex = stopsList.getSelectedIndex();
            if (selectedIndex < 0) {
                showPopupError("Select a stop to remove.");
                return;
            }

            SearchState currentState = searchViewModel.getState();
            removeMarkerController.removeAt(
                    selectedIndex,
                    currentState.getStopNames(),
                    currentState.getStops()
            );
        });
    }


    /**
     * When user types in search box, update the ViewModel's state.
     * (This keeps state consistent.)
     */
    private void attachSearchFieldListener() {
        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateState() {
                SearchState stateCopy = new SearchState(searchViewModel.getState());
                stateCopy.setLocationName(searchInputField.getText());
                searchViewModel.setState(stateCopy);
            }

            @Override public void insertUpdate(DocumentEvent e) { updateState(); }
            @Override public void removeUpdate(DocumentEvent e) { updateState(); }
            @Override public void changedUpdate(DocumentEvent e) { updateState(); }
        });

        searchInputField.addActionListener(evt -> searchButton.doClick());
    }

    private void attachGlobalEnterKey() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "globalEnter");
        this.getActionMap().put("globalEnter", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (searchInputField.isFocusOwner()) {
                    searchButton.doClick();
                } else {
                    routeButton.doClick();
                }
            }
        });
    }

    private void attachStopsListDoubleClickListener() {
        stopsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() != 2) return;

                int idx = stopsList.locationToIndex(evt.getPoint());
                java.util.List<GeoPosition> stops = searchViewModel.getState().getStops();

                if (idx >= 0 && idx < stops.size()) {
                    GeoPosition p = stops.get(idx);
                    mapPanel.setCenter(p.getLatitude(), p.getLongitude());
                }
            }
        });
    }

    /* ===== ViewModel 변경 처리 ===== */

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object value = evt.getNewValue();
        if (value instanceof SearchState state) {
            handleSearchState(state, evt.getPropertyName());
        }
    }

    private void handleSearchState(SearchState state, String propertyName) {
        if (!"state".equals(propertyName)) return;

        updateFields(state);

        stopsListModel.clear();
        for (String name : state.getStopNames()) {
            stopsListModel.addElement(name);
        }

        mapPanel.setCenter(state.getLatitude(), state.getLongitude());

        if (state.getSearchError() != null) {
            showPopupError(state.getSearchError());
        }
    }

    private void updateFields(SearchState state) {
        searchInputField.setText(state.getLocationName());
    }

    private void showPopupError(String message) {
        JLabel label = new JLabel(message);
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 150));

        Window window = SwingUtilities.getWindowAncestor(this);
        Popup popup = PopupFactory.getSharedInstance()
                .getPopup(window, label, 700, 400);
        popup.show();

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                    popup.hide();
                    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    public String getViewName() {
        return viewName;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    public void setRemoveMarkerController(RemoveMarkerController removeMarkerController) {
        this.removeMarkerController = removeMarkerController;
    }


    @Override
    public void actionPerformed(ActionEvent evt) {
        Logger logger = Logger.getLogger(getClass().getName());
        logger.info("Click " + evt.getActionCommand());
    }
}
