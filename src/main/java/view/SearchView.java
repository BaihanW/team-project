package view;

import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.search.SearchController;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.generate_route.GenerateRouteViewModel;
import interface_adapter.generate_route.GenerateRouteState;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * SearchView
 * A Swing UI panel that displays:
 * - A search bar (text field + search button)
 * - A list of stops
 * - A map with zoom controls
 * <p>
 * Responsibilities:
 * - Render UI and react to UI events
 * - Notify the SearchController when the user initiates a search
 * - Listen to SearchViewModel changes and update UI accordingly
 */
public class SearchView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName;
    private final transient SearchViewModel searchViewModel;

    // UI controls
    private final JTextField searchInputField = new JTextField(15);
    private final JButton searchButton = new JButton("Search");
    private final JButton routeButton = new JButton("Route");
    private final JButton saveButton = new JButton("Save");
    private final JButton moveUpButton = new JButton("Up");
    private final JButton moveDownButton = new JButton("Down");
    private final JButton removeButton = new JButton("Remove");

    // Controller
    private transient SearchController searchController = null;
    private transient SaveStopsController saveStopsController = null;
    private transient RemoveMarkerController removeMarkerController = null;
    private transient GenerateRouteController generateRouteController = null;

    // Map panel
    private final MapPanel mapPanel = new MapPanel();

    // Stop list UI
    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);

    public SearchView(SearchViewModel searchViewModel, GenerateRouteViewModel routeViewModel) {

        this.viewName = searchViewModel.getViewName();
        this.searchViewModel = searchViewModel;

        this.searchViewModel.addPropertyChangeListener(this);
        routeViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        // Build and attach UI components
        JSplitPane layoutSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftSidebar(), buildRightMapPanel());
        layoutSplit.setDividerLocation(350);
        layoutSplit.setOneTouchExpandable(true);

        add(layoutSplit, BorderLayout.CENTER);
        attachGlobalEnterKey();
        attachStopsListDoubleClickListener();
        attachSearchFieldListener();
        attachSearchButtonListener();
        attachSaveButtonListener();
        attachRemoveButtonListener();
        attachRouteButtonListener();
    }

    /* --------------------------------------------------------------------- */
    /* UI BUILDERS                                                           */
    /* --------------------------------------------------------------------- */

    /**
     * Build the left sidebar that contains:
     * - Search bar
     * - Stop list
     * - Reorder/remove buttons
     */
    private JPanel buildLeftSidebar() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(350, 800));
        left.setBackground(new Color(70, 130, 180));

        left.add(buildSearchSection(), BorderLayout.NORTH);
        left.add(buildStopsListSection(), BorderLayout.CENTER);
        left.add(buildStopsControlSection(), BorderLayout.SOUTH);

        return left;
    }

    /**
     * Build the search bar (text field + buttons).
     */
    private JPanel buildSearchSection() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(routeButton);
        buttons.add(saveButton);

        container.add(searchInputField, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.EAST);

        return container;
    }

    /**
     * Stop list section with custom renderer.
     */
    private JScrollPane buildStopsListSection() {
        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stopsList.setBackground(new Color(220, 235, 245));
        stopsList.setCellRenderer(new StopCellRenderer(stopsListModel));

        JScrollPane scroll = new JScrollPane(stopsList);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scroll;
    }

    /**
     * Buttons for stop reordering/removal.
     */
    private JPanel buildStopsControlSection() {
        JPanel controls = new JPanel(new GridLayout(1, 3, 5, 5));
        controls.setOpaque(false);

        controls.add(moveUpButton);     // Placeholder for clean architecture hooks
        controls.add(moveDownButton);
        controls.add(removeButton);

        return controls;
    }

    /**
     * Build right side map container.
     */
    private JPanel buildRightMapPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.add(mapPanel, BorderLayout.CENTER);

        return right;
    }

    /* --------------------------------------------------------------------- */
    /* EVENT ATTACHMENTS                                                     */
    /* --------------------------------------------------------------------- */

    private void attachRouteButtonListener() {
        routeButton.addActionListener(evt -> {
            if (generateRouteController == null) return;

            List<GeoPosition> stops = searchViewModel.getState().getStops();
            generateRouteController.generate("walking", stops);
        });
    }

    private void attachSearchButtonListener() {
        searchButton.addActionListener(evt -> {
            if (searchController == null) return;
            String text = searchViewModel.getState().getLocationName();
            searchController.execute(text);
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

    private void attachSaveButtonListener() {
        saveButton.addActionListener(e -> {
            SearchState s = searchViewModel.getState();
            saveStopsController.execute(s.getStopNames(), s.getStops());
        });
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
                List<GeoPosition> stops = searchViewModel.getState().getStops();

                if (idx >= 0 && idx < stops.size()) {
                    GeoPosition p = stops.get(idx);
                    mapPanel.setCenter(p.getLatitude(), p.getLongitude());
                }
            }
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
    
    /* --------------------------------------------------------------------- */
    /* PROPERTY CHANGE HANDLING                                              */
    /* --------------------------------------------------------------------- */

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        Object newValue = evt.getNewValue();

        // 1. Save success / error (newValue is String)
        if ("save_success".equals(property)) {
            JOptionPane.showMessageDialog(
                    this,newValue,
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if ("save_error".equals(property)) {
            JOptionPane.showMessageDialog(
                    this,
                    newValue,
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 2. SearchState updates (newValue MUST be SearchState)
        if ("state".equals(property)) {
            SearchState state = (SearchState) newValue;
            handleSearchState(state);
        }

        if (evt.getNewValue() instanceof GenerateRouteState) {
            GenerateRouteState state = (GenerateRouteState) evt.getNewValue();

            if ("route".equals(evt.getPropertyName())) {
                mapPanel.setRouteSegments(state.getRouteSegments());
            }
            if ("error".equals(evt.getPropertyName())) {
                JOptionPane.showMessageDialog(this, state.getErrorMessage());
            }
        }
    }


    private void handleSearchState(SearchState state) {

        // 1. update text field
        updateFields(state);

        // 2. update stop list
        stopsListModel.clear();
        for (String name : state.getStopNames()) {
            stopsListModel.addElement(name);
        }

        // 3. update map center
        mapPanel.setCenter(state.getLatitude(), state.getLongitude());

        // 4. show error popup if needed
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

    /* --------------------------------------------------------------------- */
    /* GETTERS / SETTERS                                                     */
    /* --------------------------------------------------------------------- */

    public String getViewName() {
        return viewName;
    }

    public void setGenerateRouteController(GenerateRouteController generateRouteController) {
        this.generateRouteController = generateRouteController;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    public void setSaveStopsController(SaveStopsController saveStopsController) {
        this.saveStopsController = saveStopsController;
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
