package view;

import interface_adapter.reorder.ReorderController;
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

import interface_adapter.generate_route.GenerateRouteController;
import interface_adapter.generate_route.GenerateRouteViewModel;
import interface_adapter.generate_route.GenerateRouteState;

import org.jxmapviewer.viewer.GeoPosition;
import java.util.ArrayList;

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
    private final DefaultListModel<String> suggestionListModel = new DefaultListModel<>();
    private final JList<String> suggestionList = new JList<>(suggestionListModel);
    private final Timer suggestionDebounceTimer;

    // Controllers
    private transient SearchController searchController = null;
    private transient SaveStopsController saveStopsController = null;
    private transient RemoveMarkerController removeMarkerController = null;
    private transient GenerateRouteController generateRouteController = null;
    private transient SuggestionController suggestionController = null;
    private transient ReorderController reorderController = null;

    // Map panel
    private final MapPanel mapPanel = new MapPanel();

    // Stop list UI
    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);

    // UI update guard
    private boolean updatingFromModel = false;

    public SearchView(SearchViewModel searchViewModel, GenerateRouteViewModel routeViewModel) {

        this.viewName = searchViewModel.getViewName();
        this.searchViewModel = searchViewModel;

        this.searchViewModel.addPropertyChangeListener(this);
        routeViewModel.addPropertyChangeListener(this);

        this.suggestionDebounceTimer = new Timer(250, evt -> {
            if (suggestionController != null) {
                suggestionController.execute(searchInputField.getText());
            }
        });
        this.suggestionDebounceTimer.setRepeats(false);

        setLayout(new BorderLayout());

        // === 지도 클릭 시 stop + 마커 추가 (장소 이름은 search bar 텍스트 사용) ===
        mapPanel.setOnMapClickListener(gp -> {
            SearchState current = new SearchState(searchViewModel.getState());

            List<String> names = new ArrayList<>(current.getStopNames());
            List<GeoPosition> stops = new ArrayList<>(current.getStops());

            String baseName = current.getLocationName();
            if (baseName == null || baseName.isBlank()) {
                baseName = "Marker " + (names.size() + 1);
            }

            names.add(baseName);
            stops.add(gp);

            current.setStopNames(names);
            current.setStops(stops);
            current.setLatitude(gp.getLatitude());
            current.setLongitude(gp.getLongitude());
            current.setLocationName(baseName);   // search bar 에도 같은 이름 유지

            searchViewModel.setState(current);
            searchViewModel.firePropertyChange();
        });

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
        attachRemoveButtonListener();
        attachMoveUpButtonListener();
        attachMoveDownButtonListener();
        attachSuggestionListListeners();
        attachSaveButtonListener();
        attachRemoveButtonListener();
        attachRouteButtonListener();
        attachSuggestionListListeners();
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

        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
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
            SearchState s = searchViewModel.getState();
            saveStopsController.execute(s.getStopNames(), s.getStops());
        });
    }

    private void attachMoveUpButtonListener() {
        moveUpButton.addActionListener(evt -> moveSelected(-1));
    }

    private void attachMoveDownButtonListener() {
        moveDownButton.addActionListener(evt -> moveSelected(1));
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

        if (evt.getNewValue() instanceof GenerateRouteState) {
            GenerateRouteState state = (GenerateRouteState) evt.getNewValue();

            if ("route".equals(evt.getPropertyName())) {
                mapPanel.setRouteSegments(state.getRouteSegments());
            }
            if ("error".equals(evt.getPropertyName())) {
                JOptionPane.showMessageDialog(this, state.getErrorMessage());
            }
        }

        if ("state".equals(property)) {
            SearchState state = (SearchState) newValue;
            handleSearchState(state);
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
        mapPanel.setStops(state.getStops());

        if (state.getSearchError() != null) showPopupError(state.getSearchError());
        if (state.getErrorMessage() != null) showPopupError(state.getErrorMessage());
        if (state.getSuggestionError() != null) showPopupError(state.getSuggestionError());

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

    public String getViewName() {
        return viewName;
    }

    public void setGenerateRouteController(GenerateRouteController generateRouteController) {
        this.generateRouteController = generateRouteController;
    }

    public void setRemoveMarkerController(RemoveMarkerController removeMarkerController) {
        this.removeMarkerController = removeMarkerController;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    public void setSaveStopsController(SaveStopsController saveStopsController) {
        this.saveStopsController = saveStopsController;
    }

    public void setSuggestionController(SuggestionController suggestionController) {
        this.suggestionController = suggestionController;
    }

    public void setReorderController(ReorderController reorderController) {
        this.reorderController = reorderController;
    }

    private void moveSelected(int delta) {
        int idx = stopsList.getSelectedIndex();
        if (idx == -1) return;
        int newIdx = idx + delta;
        if (reorderController != null) {
            reorderController.move(idx, newIdx, searchViewModel.getState().getStopNames(),
                    searchViewModel.getState().getStops());
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Logger logger = Logger.getLogger(getClass().getName());
        logger.info("Click " + evt.getActionCommand());
    }
}
