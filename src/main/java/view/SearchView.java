package view;

import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.generate_route.GenerateRouteState;
import interface_adapter.generate_route.GenerateRouteViewModel;
import interface_adapter.save_stops.SaveStopsController;
import interface_adapter.search.SearchController;
import interface_adapter.remove_marker.RemoveMarkerController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import interface_adapter.suggestion.SuggestionController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import org.jxmapviewer.viewer.GeoPosition;

public class SearchView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName;
    private final transient SearchViewModel searchViewModel;
    private final transient GenerateRouteViewModel generateRouteViewModel;

    // UI controls
    private final JTextField searchInputField = new JTextField(15);
    private final JButton searchButton = new JButton("Search");
    private final JButton routeButton = new JButton("Route");
    private final JButton moveUpButton = new JButton("Up");
    private final JButton saveButton = new JButton("Save");
    private final JButton moveDownButton = new JButton("Down");
    private final JButton removeButton = new JButton("Remove");

    private final DefaultListModel<String> suggestionListModel = new DefaultListModel<>();
    private final JList<String> suggestionList = new JList<>(suggestionListModel);
    private final Timer suggestionDebounceTimer;

    // Controllers
    private transient SearchController searchController = null;
    private transient RemoveMarkerController removeMarkerController = null;
    private transient SuggestionController suggestionController = null;
    private transient SaveStopsController saveStopsController = null;
    private transient GenerateRouteController generateRouteController = null;

    // Map panel
    private final MapPanel mapPanel = new MapPanel();

    // Stop list UI
    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);

    // UI update guard
    private boolean updatingFromModel = false;

    public SearchView(SearchViewModel searchViewModel,
                      GenerateRouteViewModel generateRouteViewModel) {

        this.viewName = searchViewModel.getViewName();
        this.searchViewModel = searchViewModel;
        this.generateRouteViewModel = generateRouteViewModel;

        this.searchViewModel.addPropertyChangeListener(this);
        this.generateRouteViewModel.addPropertyChangeListener(this);

        suggestionDebounceTimer = new Timer(250, evt -> {
            if (suggestionController != null) {
                suggestionController.execute(searchInputField.getText());
            }
        });
        suggestionDebounceTimer.setRepeats(false);

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
        attachSuggestionListListeners();
        attachSaveButtonListener();
        attachRouteButtonListener();
    }

    /* --------------------------------------------------------------------- */
    /* UI BUILDERS                                                           */
    /* --------------------------------------------------------------------- */

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

        JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 5));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(routeButton);
        buttons.add(saveButton);

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.add(searchInputField, BorderLayout.CENTER);
        searchRow.add(buttons, BorderLayout.EAST);

        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setBackground(new Color(240, 248, 255));
        suggestionList.setVisibleRowCount(5);

        JScrollPane suggestionsScroll = new JScrollPane(suggestionList);
        suggestionsScroll.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        suggestionsScroll.setPreferredSize(new Dimension(0, 140));

        container.add(searchRow, BorderLayout.NORTH);
        container.add(suggestionsScroll, BorderLayout.CENTER);

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

        controls.add(moveUpButton);
        controls.add(moveDownButton);
        controls.add(removeButton);

        return controls;
    }

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

    private void attachSearchFieldListener() {
        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateState() {
                if (updatingFromModel) return;

                SearchState stateCopy = new SearchState(searchViewModel.getState());
                stateCopy.setLocationName(searchInputField.getText());
                searchViewModel.setState(stateCopy);
                suggestionDebounceTimer.restart();
            }

            @Override public void insertUpdate(DocumentEvent e) { updateState(); }
            @Override public void removeUpdate(DocumentEvent e) { updateState(); }
            @Override public void changedUpdate(DocumentEvent e) { updateState(); }
        });

        searchInputField.addActionListener(evt -> searchButton.doClick());
    }

    private void attachSaveButtonListener() {
        saveButton.addActionListener(e -> {
            if (saveStopsController == null) return;
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

                if (stops != null && idx >= 0 && idx < stops.size()) {
                    GeoPosition p = stops.get(idx);
                    mapPanel.setCenter(p.getLatitude(), p.getLongitude());
                }
            }
        });
    }

    private void attachSuggestionListListeners() {
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evt) {
                if (suggestionList.locationToIndex(evt.getPoint()) >= 0) {
                    applySelectedSuggestion();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applySelectedSuggestion();
                }
            }
        });
    }

    private void applySelectedSuggestion() {
        String selection = suggestionList.getSelectedValue();
        if (selection == null) return;

        updatingFromModel = true;
        try {
            searchInputField.setText(selection);
            int caret = selection.length();
            searchInputField.setCaretPosition(caret);
            searchInputField.select(caret, caret);
        } finally {
            updatingFromModel = false;
        }
        SearchState updatedState = new SearchState(searchViewModel.getState());
        updatedState.setLocationName(selection);
        searchViewModel.setState(updatedState);
        searchButton.doClick();
    }

    /* --------------------------------------------------------------------- */
    /* PROPERTY CHANGE HANDLING                                              */
    /* --------------------------------------------------------------------- */

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        Object newValue = evt.getNewValue();

        if ("save_success".equals(property)) {
            JOptionPane.showMessageDialog(
                    this,
                    newValue,
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

        if (!"state".equals(property)) {
            return;
        }

        if (newValue instanceof SearchState state) {
            handleSearchState(state);
        } else if (newValue instanceof GenerateRouteState routeState) {
            handleRouteState(routeState);
        }
    }

    private void handleSearchState(SearchState state) {
        updateFields(state);

        stopsListModel.clear();
        for (String name : state.getStopNames()) {
            stopsListModel.addElement(name);
        }

        suggestionListModel.clear();
        for (String suggestion : state.getSuggestions()) {
            suggestionListModel.addElement(suggestion);
        }

        mapPanel.setCenter(state.getLatitude(), state.getLongitude());

        if (state.getSearchError() != null) showPopupError(state.getSearchError());
        if (state.getErrorMessage() != null) showPopupError(state.getErrorMessage());
        if (state.getSuggestionError() != null) showPopupError(state.getSuggestionError());
    }

    private void handleRouteState(GenerateRouteState routeState) {
        // 지금은 경로 에러만 보여주고, 실제 선 그리기는 MapPanel 쪽 API 있으면 거기에 맞춰 추가하면 됨.
        if (routeState.getErrorMessage() != null) {
            showPopupError(routeState.getErrorMessage());
        }
        // routeState.getRouteSegments() 를 mapPanel에 넘겨서 선을 그리고 싶으면
        // 나중에 mapPanel에 전용 메서드 추가해서 여기서 호출하면 됨.
    }

    private void updateFields(SearchState state) {
        String newText = state.getLocationName() == null ? "" : state.getLocationName();
        String currentText = searchInputField.getText();

        if (newText.equals(currentText)) {
            updatingFromModel = true;
            try {
                int caret = newText.length();
                searchInputField.setCaretPosition(caret);
                searchInputField.select(caret, caret);
            } finally {
                updatingFromModel = false;
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            updatingFromModel = true;
            try {
                searchInputField.setText(newText);
                int caret = newText.length();
                searchInputField.setCaretPosition(caret);
                searchInputField.select(caret, caret);
            } finally {
                updatingFromModel = false;
            }
        });
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

    public void setRemoveMarkerController(RemoveMarkerController removeMarkerController) {
        this.removeMarkerController = removeMarkerController;
    }

    public void setSuggestionController(SuggestionController suggestionController) {
        this.suggestionController = suggestionController;
    }

    public void setSaveStopsController(SaveStopsController saveStopsController) {
        this.saveStopsController = saveStopsController;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Logger logger = Logger.getLogger(getClass().getName());
        logger.info("Click " + evt.getActionCommand());
    }

    public void showMarkersForCurrentStops() {
        SearchState state = searchViewModel.getState();
        List<GeoPosition> stops = state.getStops();
        if (stops == null) {
            return;
        }

        for (GeoPosition p : stops) {
            mapPanel.addMarker(p.getLatitude(), p.getLongitude());
        }
    }
}


