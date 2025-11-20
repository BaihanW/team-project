package view;

import data_access.RoutingDataAccessObject;
import data_access.OSMDataAccessObject;
import interface_adapter.search.SearchController;
import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;

public class SearchView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName;
    final JPanel searchLocationPanel = new JPanel(new BorderLayout());
    private final JTextField searchInputField = new JTextField(15);
    private final JButton search =  new JButton("Search");
    private final JButton routeButton = new JButton("Route");
    private transient SearchController searchController = null;
    private final MapPanel mapPanel = new MapPanel();

    private RoutingDataAccessObject routingDao = null;
    private OSMDataAccessObject osmDao = null;

    private final DefaultListModel<String> stopsListModel = new DefaultListModel<>();
    private final JList<String> stopsList = new JList<>(stopsListModel);
    private final List<GeoPosition> stopPositions = new ArrayList<>();
    private final List<entity.Location> currentSuggestions = new ArrayList<>();

    public SearchView(SearchViewModel searchViewModel) {
        this.viewName = searchViewModel.getViewName();
        searchViewModel.addPropertyChangeListener(this);

        search.addActionListener(
                evt -> {
                    if (evt.getSource().equals(search)) {
                        final SearchState currentState = searchViewModel.getState();

                        searchController.execute(
                                currentState.getLocationName()
                        );
                    }
                }
        );

        routeButton.addActionListener(evt -> computeAndDisplayRoute());

        searchInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final SearchState currentState = searchViewModel.getState();
                currentState.setLocationName(searchInputField.getText());
                searchViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentListenerHelper();
            }
        });

        this.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(70,130,180)); // steel blue
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 800)); // will be approx 1/3 for common window sizes

        JPanel topSearch = new JPanel(new BorderLayout());
        topSearch.setOpaque(false);
        topSearch.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topSearch.add(searchInputField, BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(1,2,5,5));
        btns.setOpaque(false);
        btns.add(search);
        btns.add(routeButton);
        topSearch.add(btns, BorderLayout.EAST);

        leftPanel.add(topSearch, BorderLayout.NORTH);

        final JPopupMenu suggestionPopup = new JPopupMenu();
        final JList<String> suggestionList = new JList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        suggestionPopup.add(new JScrollPane(suggestionList));

        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int idx = suggestionList.locationToIndex(evt.getPoint());
                if (idx >= 0 && idx < currentSuggestions.size()) {
                    entity.Location chosen = currentSuggestions.get(idx);
                    addStop(chosen.getName(), new GeoPosition(chosen.getLatitude(), chosen.getLongitude()));
                    suggestionPopup.setVisible(false);
                }
            }
        });
        suggestionList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    int idx = suggestionList.getSelectedIndex();
                    if (idx >= 0 && idx < currentSuggestions.size()) {
                        entity.Location chosen = currentSuggestions.get(idx);
                        addStop(chosen.getName(), new GeoPosition(chosen.getLatitude(), chosen.getLongitude()));
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        searchInputField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && suggestionPopup.isVisible()) {
                    suggestionList.requestFocusInWindow();
                    suggestionList.setSelectedIndex(0);
                }
            }
        });

        searchInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateSuggestions() {
                String q = searchInputField.getText();
                if (osmDao == null || q == null || q.isBlank()) {
                    suggestionPopup.setVisible(false);
                    return;
                }
                org.jxmapviewer.viewer.GeoPosition center = mapPanel.getMapViewer().getAddressLocation();
                double lat = center.getLatitude();
                double lon = center.getLongitude();
                double delta = 0.15;
                double minLat = lat - delta;
                double maxLat = lat + delta;
                double minLon = lon - delta;
                double maxLon = lon + delta;

                SwingWorker<List<entity.Location>, Void> worker = new SwingWorker<>() {
                    @Override
                    protected List<entity.Location> doInBackground() throws Exception {
                        try {
                            return osmDao.searchSuggestions(q, minLon, minLat, maxLon, maxLat, 6);
                        } catch (Exception e) {
                            return java.util.Collections.emptyList();
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            List<entity.Location> res = get();
                            currentSuggestions.clear();
                            if (res == null || res.isEmpty()) {
                                suggestionPopup.setVisible(false);
                                return;
                            }
                            currentSuggestions.addAll(res);
                            DefaultListModel<String> model = new DefaultListModel<>();
                            for (entity.Location l : res) model.addElement(l.getName());
                            suggestionList.setModel(model);

                            suggestionPopup.pack();
                            suggestionPopup.show(searchInputField, 0, searchInputField.getHeight());
                            searchInputField.requestFocusInWindow();
                        } catch (Exception e) {
                            suggestionPopup.setVisible(false);
                        }
                    }
                };
                worker.execute();
            }

            @Override
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }

            @Override
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }

            @Override
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }
        });

        leftPanel.add(topSearch, BorderLayout.NORTH);

        // stops list center
        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stopsList.setBackground(new Color(220, 235, 245));
        // custom renderer to show number to the left of each name and draw connecting lines
        stopsList.setCellRenderer(new ListCellRenderer<String>() {
            private final Font font = new JLabel().getFont().deriveFont(14f);

            class CellPanel extends JPanel {
                private String name = "";
                private int index = 0;
                private boolean isSelected = false;

                public CellPanel() {
                    setOpaque(true);
                    setPreferredSize(new Dimension(0, 52)); // increase vertical spacing
                }

                public void setData(String name, int index, boolean isSelected) {
                    this.name = name;
                    this.index = index;
                    this.isSelected = isSelected;
                    setBackground(isSelected ? new Color(200, 220, 240) : new Color(220, 235, 245));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // badge parameters
                    int radius = 14;
                    int badgeX = 12;
                    int centerX = badgeX + radius;
                    int centerY = h / 2;

                    // draw connecting lines (top and bottom) with route color and per-segment alpha
                    int n = stopsListModel.getSize();
                    int segments = Math.max(0, n - 1);
                    Color routeColor = new Color(0, 120, 255);

                    // draw top connector (connect previous to this)
                    if (index > 0 && segments > 0) {
                        int segIndex = index - 1; // segment between index-1 and index
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, 0, centerX, centerY - radius);
                    }

                    // draw bottom connector (connect this to next)
                    if (index < n - 1 && segments > 0) {
                        int segIndex = index; // segment between index and index+1
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, centerY + radius, centerX, h);
                    }

                    // draw badge (dark background)
                    int bx = badgeX;
                    int by = centerY - radius;
                    g2.setColor(new Color(100, 100, 140));
                    g2.fillOval(bx, by, radius * 2, radius * 2);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(bx, by, radius * 2, radius * 2);

                    // draw number
                    String num = String.valueOf(index + 1);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = bx + (radius * 2 - fm.stringWidth(num)) / 2;
                    int ty = by + ((radius * 2 - fm.getHeight()) / 2) + fm.getAscent();
                    g2.setColor(Color.WHITE);
                    g2.drawString(num, tx, ty);

                    // draw name text
                    g2.setColor(Color.DARK_GRAY);
                    int nameX = bx + radius * 2 + 12;
                    int nameY = centerY + fm.getAscent() / 2 - 2;
                    g2.drawString(name, nameX, nameY);

                    g2.dispose();
                }
            }

            private final CellPanel panel = new CellPanel();

            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                panel.setData(value == null ? "" : value, index, isSelected);
                return panel;
            }
        });
        JScrollPane listScroll = new JScrollPane(stopsList);
        listScroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1,3,5,5));
        controls.setOpaque(false);
        JButton up = new JButton("Up");
        JButton down = new JButton("Down");
        JButton remove = new JButton("Remove");
        controls.add(up);
        controls.add(down);
        controls.add(remove);
        leftPanel.add(controls, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(350);
        split.setResizeWeight(0);
        split.setOneTouchExpandable(true);
        this.add(split, BorderLayout.CENTER);

        up.addActionListener(e -> moveSelected(-1));
        down.addActionListener(e -> moveSelected(1));
        remove.addActionListener(e -> removeSelected());

        mapPanel.setClickListener(gp -> {
            String name = String.format("%.5f, %.5f", gp.getLatitude(), gp.getLongitude());
            GeoPosition usePos = gp;
            if (osmDao != null) {
                try {
                    entity.Location loc = osmDao.reverse(gp.getLatitude(), gp.getLongitude());
                    if (loc != null) {
                        name = loc.getName();
                        usePos = new GeoPosition(loc.getLatitude(), loc.getLongitude());
                    }
                } catch (Exception ex) {
                }
            }
            addStop(name, usePos);
        });

        stopsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int idx = stopsList.locationToIndex(evt.getPoint());
                    if (idx >= 0 && idx < stopPositions.size()) {
                        GeoPosition p = stopPositions.get(idx);
                        mapPanel.setCenter(p.getLatitude(), p.getLongitude());
                    }
                }
            }
        });
    }

    private void computeAndDisplayRoute() {
        if (routingDao == null) {
            JOptionPane.showMessageDialog(this, "Routing backend not configured.");
            return;
        }

        if (stopPositions.size() < 2) {
            JOptionPane.showMessageDialog(this, "Add at least two stops to compute a full route.");
            return;
        }

        SwingWorker<List<List<org.jxmapviewer.viewer.GeoPosition>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<List<org.jxmapviewer.viewer.GeoPosition>> doInBackground() throws Exception {
                List<List<org.jxmapviewer.viewer.GeoPosition>> segments = new ArrayList<>();
                for (int i = 0; i < stopPositions.size() - 1; i++) {
                    org.jxmapviewer.viewer.GeoPosition a = stopPositions.get(i);
                    org.jxmapviewer.viewer.GeoPosition b = stopPositions.get(i + 1);
                    try {
                        List<org.jxmapviewer.viewer.GeoPosition> segment = routingDao.getRoute(a, b, "walking");
                        if (segment != null && !segment.isEmpty()) {
                            segments.add(segment);
                        } else {
                            List<org.jxmapviewer.viewer.GeoPosition> straight = new ArrayList<>();
                            straight.add(a);
                            straight.add(b);
                            segments.add(straight);
                        }
                    } catch (Exception e) {
                        List<org.jxmapviewer.viewer.GeoPosition> straight = new ArrayList<>();
                        straight.add(a);
                        straight.add(b);
                        segments.add(straight);
                    }
                }
                return segments;
            }

            @Override
            protected void done() {
                try {
                    List<List<org.jxmapviewer.viewer.GeoPosition>> segs = get();
                    if (segs == null || segs.isEmpty()) {
                        JOptionPane.showMessageDialog(SearchView.this, "No route found.");
                    } else {
                        mapPanel.setRouteSegments(segs);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SearchView.this, "Routing error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void addStop(String name, GeoPosition gp) {
        stopsListModel.addElement(name);
        stopPositions.add(gp);
        mapPanel.addStop(gp.getLatitude(), gp.getLongitude());
    }

    private void moveSelected(int delta) {
        int idx = stopsList.getSelectedIndex();
        if (idx == -1) return;
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= stopsListModel.getSize()) return;
        String item = stopsListModel.get(idx);
        GeoPosition pos = stopPositions.get(idx);
        stopsListModel.remove(idx);
        stopPositions.remove(idx);
        stopsListModel.add(newIdx, item);
        stopPositions.add(newIdx, pos);
        stopsList.setSelectedIndex(newIdx);
        mapPanel.setStops(stopPositions);
        computeAndDisplayRouteIfAuto();
    }

    private void removeSelected() {
        int idx = stopsList.getSelectedIndex();
        if (idx == -1) return;
        stopsListModel.remove(idx);
        stopPositions.remove(idx);
        mapPanel.setStops(stopPositions);
        computeAndDisplayRouteIfAuto();
    }

    private void computeAndDisplayRouteIfAuto() {
        if (routingDao != null && stopPositions.size() >= 2) {
            computeAndDisplayRoute();
        } else if (stopPositions.size() < 2) {
            mapPanel.clearRoute();
        }
    }

    public void actionPerformed(ActionEvent evt) {
        System.out.println("Click " + evt.getActionCommand());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final SearchState state = (SearchState) evt.getNewValue();
        setFields(state);
        if (state.getSearchError() != null) {
            JLabel label = new JLabel(state.getSearchError());
            label.setOpaque(true);
            label.setBackground(new Color(255, 255, 150));
            Window window = SwingUtilities.getWindowAncestor(this);
            Popup popup = PopupFactory.getSharedInstance()
                    .getPopup(window, label, 700, 400);
            popup.show();
            Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                        popup.hide();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    }
                }
            }, AWTEvent.MOUSE_EVENT_MASK);
        } else {
            mapPanel.setCenter(state.getLatitude(), state.getLongitude());
            addStop(state.getLocationName(), new GeoPosition(state.getLatitude(), state.getLongitude()));
            computeAndDisplayRouteIfAuto();
        }
    }

    private void setFields(SearchState state) {
        searchInputField.setText(state.getLocationName());
    }

    public String getViewName() {
        return viewName;
    }

    public void setSearchController(SearchController searchController) {
        this.searchController = searchController;
    }

    public void setRoutingDataAccessObject(RoutingDataAccessObject routingDao) {
        this.routingDao = routingDao;
    }

    public void setOsmDataAccessObject(OSMDataAccessObject osmDao) {
        this.osmDao = osmDao;
    }

}
