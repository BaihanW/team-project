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
import java.awt.event.KeyEvent;
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
    private boolean routeHasBeenComputed = false;
    private final List<GeoPosition> routedStops = new ArrayList<>();

    private final JPanel progressPanelContainer;
    private final JProgressBar rerouteProgressBar;
    private final JLabel rerouteLabel;
    private javax.swing.Timer progressTimer;
    private int fakeProgress = 0;

    private final JPanel searchProgressContainer;
    private final JLabel searchProgressLabel;
    private final JProgressBar searchProgressBar;
    private SwingWorker<List<entity.Location>, Void> suggestionWorker = null;

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

        searchProgressLabel = new JLabel("Searching the area...");
        searchProgressLabel.setFont(searchProgressLabel.getFont().deriveFont(Font.PLAIN, 12f));
        searchProgressBar = new JProgressBar();
        searchProgressBar.setIndeterminate(true);
        searchProgressBar.setPreferredSize(new Dimension(120, 12));
        searchProgressContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        searchProgressContainer.setOpaque(false);
        searchProgressContainer.add(searchProgressLabel);
        searchProgressContainer.add(searchProgressBar);
        JButton leftZoomBtn = new JButton("+");
        leftZoomBtn.setToolTipText("Zoom in");
        leftZoomBtn.setPreferredSize(new Dimension(36, 24));
        leftZoomBtn.setFont(leftZoomBtn.getFont().deriveFont(Font.BOLD, 14f));
        leftZoomBtn.setMargin(new Insets(2, 4, 2, 4));
        leftZoomBtn.setHorizontalAlignment(SwingConstants.CENTER);
        leftZoomBtn.addActionListener(evt -> {
            try {
                JComponent viewer = mapPanel.getMapViewer();
                GeoPosition center = mapPanel.getMapViewer().convertPointToGeoPosition(new Point(viewer.getWidth()/2, viewer.getHeight()/2));
                int z = mapPanel.getMapViewer().getZoom();
                int newZ = Math.max(0, z - 1);
                mapPanel.getMapViewer().setZoom(newZ);
                mapPanel.getMapViewer().setAddressLocation(center);
            } catch (Exception ignored) {}
        });

        JButton rightZoomBtn = new JButton("-");
        rightZoomBtn.setToolTipText("Zoom out");
        rightZoomBtn.setPreferredSize(new Dimension(36, 24));
        rightZoomBtn.setFont(rightZoomBtn.getFont().deriveFont(Font.BOLD, 14f));
        rightZoomBtn.setMargin(new Insets(2, 4, 2, 4));
        rightZoomBtn.setHorizontalAlignment(SwingConstants.CENTER);
        rightZoomBtn.addActionListener(evt -> {
            try {
                JComponent viewer = mapPanel.getMapViewer();
                GeoPosition center = mapPanel.getMapViewer().convertPointToGeoPosition(new Point(viewer.getWidth()/2, viewer.getHeight()/2));
                int z = mapPanel.getMapViewer().getZoom();
                int newZ = Math.min(20, z + 1);
                mapPanel.getMapViewer().setZoom(newZ);
                mapPanel.getMapViewer().setAddressLocation(center);
            } catch (Exception ignored) {}
        });

        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        zoomPanel.setOpaque(false);
        zoomPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        searchProgressContainer.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        leftZoomBtn.setText("+");
        rightZoomBtn.setText("-");

        zoomPanel.add(leftZoomBtn);
        zoomPanel.add(rightZoomBtn);
        searchProgressContainer.add(zoomPanel);

        searchProgressContainer.setPreferredSize(new Dimension(0, 44));
        searchProgressContainer.setVisible(true);
        searchProgressLabel.setText("");
        searchProgressBar.setVisible(false);


        this.add(searchProgressContainer, BorderLayout.NORTH);


        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(70,130,180));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 800));

        JPanel topSearch = new JPanel(new BorderLayout());
        topSearch.setOpaque(false);
        topSearch.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topSearch.add(searchInputField, BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(1,2,5,5));
        btns.setOpaque(false);
        btns.add(search);
        btns.add(routeButton);
        topSearch.add(btns, BorderLayout.EAST);

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

        searchInputField.addActionListener(evt -> {
            if (suggestionList.isFocusOwner()) return;
            search.doClick();
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

                if (suggestionWorker != null && !suggestionWorker.isDone()) {
                    suggestionWorker.cancel(true);
                }

                SwingUtilities.invokeLater(() -> {
                    searchProgressLabel.setText("Searching the area...");
                    searchProgressBar.setVisible(true);
                });

                suggestionWorker = new SwingWorker<>() {
                    @Override
                    protected List<entity.Location> doInBackground() throws Exception {
                        try {
                            List<entity.Location> res = osmDao.searchSuggestions(q, minLon, minLat, maxLon, maxLat, 6);
                            if (isCancelled()) return java.util.Collections.emptyList();
                            return res;
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
                        } finally {
                            // hide search progress UI
                            SwingUtilities.invokeLater(() -> {
                                searchProgressBar.setVisible(false);
                                searchProgressLabel.setText("");
                            });
                        }
                    }
                };
                suggestionWorker.execute();
            }

            @Override
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }

            @Override
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }

            @Override
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }
        });

        leftPanel.add(topSearch, BorderLayout.NORTH);

        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stopsList.setBackground(new Color(220, 235, 245));
        stopsList.setCellRenderer(new ListCellRenderer<String>() {
            private final Font font = new JLabel().getFont().deriveFont(14f);

            class CellPanel extends JPanel {
                private String name = "";
                private int index = 0;
                private boolean isSelected = false;
                private boolean isRoutedAbove = false;  // Is the stop above routed to this stop?
                private boolean isRoutedBelow = false;  // Is this stop routed to the stop below?
                private boolean isStrandedAbove = false;  // Is there a stranded stop above?
                private boolean isStrandedBelow = false;  // Is there a stranded stop below?
                private int strandedAboveCount = 0;  // How many stranded stops above?
                private int strandedBelowCount = 0;  // How many stranded stops below?

                public CellPanel() {
                    setOpaque(true);
                    setPreferredSize(new Dimension(0, 52));
                }

                public void setData(String name, int index, boolean isSelected,
                                  boolean isRoutedAbove, boolean isRoutedBelow,
                                  boolean isStrandedAbove, boolean isStrandedBelow,
                                  int strandedAboveCount, int strandedBelowCount) {
                    this.name = name;
                    this.index = index;
                    this.isSelected = isSelected;
                    this.isRoutedAbove = isRoutedAbove;
                    this.isRoutedBelow = isRoutedBelow;
                    this.isStrandedAbove = isStrandedAbove;
                    this.isStrandedBelow = isStrandedBelow;
                    this.strandedAboveCount = strandedAboveCount;
                    this.strandedBelowCount = strandedBelowCount;
                    setBackground(isSelected ? new Color(200, 220, 240) : new Color(220, 235, 245));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    int radius = 14;
                    int badgeX = 12;
                    int centerX = badgeX + radius;
                    int centerY = h / 2;

                    int n = stopsListModel.getSize();
                    Color routeColor = new Color(0, 120, 255);

                    // Draw line above if routed to above
                    if (isRoutedAbove) {
                        int segIndex = index - 1;
                        int segments = Math.max(1, n - 1);
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, 0, centerX, centerY - radius);
                    }

                    // Draw line below if routed to below
                    if (isRoutedBelow) {
                        int segIndex = index;
                        int segments = Math.max(1, n - 1);
                        float t = (segments == 1) ? 0f : ((float) segIndex) / (float) (segments - 1);
                        int alphaStart = 255;
                        int alphaEnd = Math.max(1, (int) Math.round(255 * 0.30));
                        int alpha = (int) Math.round(alphaStart + t * (alphaEnd - alphaStart));
                        alpha = Math.max(0, Math.min(255, alpha));
                        g2.setColor(new Color(routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue(), alpha));
                        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(centerX, centerY + radius, centerX, h);
                    }


                    int bx = badgeX;
                    int by = centerY - radius;
                    g2.setColor(new Color(100, 100, 140));
                    g2.fillOval(bx, by, radius * 2, radius * 2);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(bx, by, radius * 2, radius * 2);

                    String num = String.valueOf(index + 1);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = bx + (radius * 2 - fm.stringWidth(num)) / 2;
                    int ty = by + ((radius * 2 - fm.getHeight()) / 2) + fm.getAscent();
                    g2.setColor(Color.WHITE);
                    g2.drawString(num, tx, ty);

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
                // Determine routing status of this and adjacent stops
                boolean isThisRouted = isStopRouted(index);
                boolean isAboveRouted = (index > 0) ? isStopRouted(index - 1) : false;
                boolean isBelowRouted = (index < stopsListModel.getSize() - 1) ? isStopRouted(index + 1) : false;

                boolean isRoutedAbove = isThisRouted && isAboveRouted && areConsecutiveStopsConnected(index - 1, index);
                boolean isRoutedBelow = isThisRouted && isBelowRouted && areConsecutiveStopsConnected(index, index + 1);

                panel.setData(value == null ? "" : value, index, isSelected,
                            isRoutedAbove, isRoutedBelow, false, false, 0, 0);
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

         JPanel progressBox = new JPanel();
         progressBox.setLayout(new BoxLayout(progressBox, BoxLayout.Y_AXIS));
         progressBox.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(new Color(180, 180, 180)),
                 BorderFactory.createEmptyBorder(8, 10, 8, 10)));
         progressBox.setBackground(new Color(255, 255, 255, 230));

        rerouteLabel = new JLabel("");
        rerouteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rerouteLabel.setFont(rerouteLabel.getFont().deriveFont(Font.PLAIN, 12f));
        progressBox.add(rerouteLabel);
        progressBox.add(Box.createRigidArea(new Dimension(0,6)));

        rerouteProgressBar = new JProgressBar(0, 100);
        rerouteProgressBar.setValue(0);
        rerouteProgressBar.setPreferredSize(new Dimension(220, 14));
        rerouteProgressBar.setForeground(new Color(0, 120, 255));
        rerouteProgressBar.setBorderPainted(false);
        rerouteProgressBar.setStringPainted(false);
        progressBox.add(rerouteProgressBar);

        JButton startNewMapButton = new JButton("Start new map");
        startNewMapButton.setToolTipText("Clear all stops and routes to start over");
        startNewMapButton.setPreferredSize(new Dimension(120, 34));
        startNewMapButton.setFont(startNewMapButton.getFont().deriveFont(Font.PLAIN, 12f));
        startNewMapButton.addActionListener(e -> clearAllStopsAndRoutes());

        progressPanelContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        progressPanelContainer.setOpaque(false);
        progressPanelContainer.add(progressBox);
        progressPanelContainer.add(startNewMapButton);
        progressPanelContainer.setPreferredSize(new Dimension(0, 72));
        progressPanelContainer.setVisible(true);
         rerouteLabel.setText("");
         rerouteProgressBar.setVisible(false);

         this.add(progressPanelContainer, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            Dimension size = rightPanel.getSize();
            if (size.width == 0 || size.height == 0) {
                size = new Dimension(Math.max(800 - 350, 400), 600);
            }
            mapPanel.setBounds(0, 0, size.width, size.height);
        });

        rightPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = rightPanel.getSize();
                mapPanel.setBounds(0, 0, size.width, size.height);
            }
        });

        up.addActionListener(e -> moveSelected(-1));
        down.addActionListener(e -> moveSelected(1));
        remove.addActionListener(e -> removeSelected());

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "globalEnter");
        this.getActionMap().put("globalEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchInputField.isFocusOwner()) {
                    search.doClick();
                    return;
                }
                if (suggestionPopup != null && suggestionPopup.isVisible()) {
                    return;
                }
                if (suggestionList != null && suggestionList.isFocusOwner()) {
                    return;
                }
                routeButton.doClick();
             }
         });

        InputMap listIm = stopsList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap listAm = stopsList.getActionMap();
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "listUp");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "listDown");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "listRemove");
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "listRemove");
        listAm.put("listUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { moveSelected(-1); }
        });
        listAm.put("listDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { moveSelected(1); }
        });
        listAm.put("listRemove", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { removeSelected(); }
        });

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

    private void showRerouteProgress() {
        SwingUtilities.invokeLater(() -> {
            fakeProgress = 0;
            rerouteProgressBar.setValue(0);
            rerouteProgressBar.setVisible(true);
            rerouteLabel.setText("Rerouting...");
             if (progressTimer != null && progressTimer.isRunning()) progressTimer.stop();
             progressTimer = new javax.swing.Timer(120, e -> {
                 int add = 3 + (int) (Math.random() * 6);
                 fakeProgress = Math.min(95, fakeProgress + add);
                 rerouteProgressBar.setValue(fakeProgress);
             });
             progressTimer.start();
         });
     }

     private void hideRerouteProgress() {
         SwingUtilities.invokeLater(() -> {
             if (progressTimer != null) {
                 progressTimer.stop();
             }
            rerouteProgressBar.setValue(100);
            javax.swing.Timer t = new javax.swing.Timer(300, e -> {
                rerouteProgressBar.setVisible(false);
                rerouteLabel.setText("");
                ((javax.swing.Timer) e.getSource()).stop();
            });
             t.setRepeats(false);
             t.start();
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

        showRerouteProgress();

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
                        routeHasBeenComputed = true;
                        routedStops.clear();
                        routedStops.addAll(stopPositions);
                        // Repaint the stops list to update sidebar display
                        SwingUtilities.invokeLater(() -> stopsList.repaint());
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SearchView.this, "Routing error: " + e.getMessage());
                } finally {
                    hideRerouteProgress();
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
        stopsList.repaint();  // Repaint to update routing display
    }

    private void removeSelected() {
        int idx = stopsList.getSelectedIndex();
        if (idx == -1) return;
        stopsListModel.remove(idx);
        stopPositions.remove(idx);
        mapPanel.setStops(stopPositions);
        computeAndDisplayRouteIfAuto();
        stopsList.repaint();  // Repaint to update routing display
    }

    private void computeAndDisplayRouteIfAuto() {
        if (routingDao != null && stopPositions.size() >= 2 && routeHasBeenComputed) {
            // Build a list of stops to route, including stranded markers that are sandwiched between routed stops
            List<GeoPosition> stopsToRoute = new ArrayList<>();

            // First, identify which positions are routed
            List<Integer> routedIndices = new ArrayList<>();
            for (int i = 0; i < stopPositions.size(); i++) {
                GeoPosition pos = stopPositions.get(i);
                for (GeoPosition routedPos : routedStops) {
                    if (geoPositionsEqual(pos, routedPos)) {
                        routedIndices.add(i);
                        break;
                    }
                }
            }

            // If we don't have at least 2 routed stops, just use the original logic
            if (routedIndices.size() < 2) {
                for (GeoPosition pos : stopPositions) {
                    for (GeoPosition routedPos : routedStops) {
                        if (geoPositionsEqual(pos, routedPos)) {
                            stopsToRoute.add(pos);
                            break;
                        }
                    }
                }
            } else {
                // Consume stranded markers sandwiched between routed markers
                for (int i = 0; i < stopPositions.size(); i++) {
                    GeoPosition pos = stopPositions.get(i);
                    boolean isRouted = routedIndices.contains(i);

                    if (isRouted) {
                        stopsToRoute.add(pos);
                    } else {
                        // Check if this stranded marker is sandwiched between two routed markers
                        boolean hasPreviousRouted = false;
                        boolean hasNextRouted = false;

                        for (int idx : routedIndices) {
                            if (idx < i) hasPreviousRouted = true;
                            if (idx > i) hasNextRouted = true;
                        }

                        // If sandwiched between routed markers, include it
                        if (hasPreviousRouted && hasNextRouted) {
                            stopsToRoute.add(pos);
                        }
                    }
                }
            }

            if (stopsToRoute.size() >= 2) {
                computeAndDisplayRouteForStops(stopsToRoute);
            } else {
                mapPanel.clearRoute();
            }
        } else if (stopPositions.size() < 2) {
            mapPanel.clearRoute();
            routeHasBeenComputed = false;
            routedStops.clear();
        }
    }

    private boolean geoPositionsEqual(GeoPosition p1, GeoPosition p2) {
        if (p1 == null || p2 == null) return false;
        double tolerance = 0.000001;
        return Math.abs(p1.getLatitude() - p2.getLatitude()) < tolerance &&
               Math.abs(p1.getLongitude() - p2.getLongitude()) < tolerance;
    }

    private void computeAndDisplayRouteForStops(List<GeoPosition> stops) {
        if (routingDao == null || stops.size() < 2) {
            return;
        }

        showRerouteProgress();

        SwingWorker<List<List<org.jxmapviewer.viewer.GeoPosition>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<List<org.jxmapviewer.viewer.GeoPosition>> doInBackground() throws Exception {
                List<List<org.jxmapviewer.viewer.GeoPosition>> segments = new ArrayList<>();
                for (int i = 0; i < stops.size() - 1; i++) {
                    org.jxmapviewer.viewer.GeoPosition a = stops.get(i);
                    org.jxmapviewer.viewer.GeoPosition b = stops.get(i + 1);
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
                    } else {
                        mapPanel.setRouteSegments(segs);
                        // Update routedStops to reflect the current stops being routed (including consumed stranded markers)
                        routedStops.clear();
                        routedStops.addAll(stops);
                    }
                } catch (Exception e) {
                } finally {
                    hideRerouteProgress();
                    // Repaint the stops list to update routing display
                    SwingUtilities.invokeLater(() -> stopsList.repaint());
                }
            }
        };
        worker.execute();
    }

    private void clearAllStopsAndRoutes() {
        stopsListModel.clear();
        stopPositions.clear();

        mapPanel.clearStops();

        searchInputField.setText("");

        currentSuggestions.clear();
        routeHasBeenComputed = false;
        routedStops.clear();
    }

    /**
     * Check if a stop at the given index is part of the routed stops
     */
    private boolean isStopRouted(int index) {
        if (index < 0 || index >= stopPositions.size()) {
            return false;
        }
        GeoPosition stopPos = stopPositions.get(index);
        for (GeoPosition routedPos : routedStops) {
            if (geoPositionsEqual(stopPos, routedPos)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Check if two consecutive stops (by index) are actually connected in the route
     */
    private boolean areConsecutiveStopsConnected(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= stopPositions.size() || toIndex >= stopPositions.size()) {
            return false;
        }
        if (!isStopRouted(fromIndex) || !isStopRouted(toIndex)) {
            return false;
        }

        GeoPosition fromPos = stopPositions.get(fromIndex);
        GeoPosition toPos = stopPositions.get(toIndex);

        // Find the indices of these positions in the routedStops list
        int fromRoutedIndex = -1;
        int toRoutedIndex = -1;

        for (int i = 0; i < routedStops.size(); i++) {
            if (geoPositionsEqual(fromPos, routedStops.get(i))) {
                fromRoutedIndex = i;
            }
            if (geoPositionsEqual(toPos, routedStops.get(i))) {
                toRoutedIndex = i;
            }
        }

        // They are connected if they are consecutive in the routed stops
        return fromRoutedIndex >= 0 && toRoutedIndex >= 0 && (toRoutedIndex == fromRoutedIndex + 1);
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
