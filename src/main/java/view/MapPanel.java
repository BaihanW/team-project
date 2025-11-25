package view;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.function.Consumer;


public class MapPanel extends JPanel {

    private static final int MAX_Z = 20;
    private static final double TRACKPAD_PINCH_STEP = 0.03;
    private static final double HARDWARE_WHEEL_STEP = 1.2;  // Doubled from 0.6 for 2x sensitivity
    private static final long PINCH_SESSION_TIMEOUT_MS = 180L;
    private static final double PINCH_ROTATION_THRESHOLD = 0.12;
    private static final long PINCH_TIME_WINDOW_MS = 60L;
    private static final int PINCH_ACTIVATION_COUNT = 1;
    private static final int PINCH_STEPS_PER_EVENT = 20;

    private final JXMapViewer mapViewer;

    private final List<GeoPosition> markerPositions = new ArrayList<>();

    private final Set<Waypoint> waypoints = new HashSet<>();

    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    private RoutePainter routePainter;
    private NumberedMarkerPainter numberedMarkerPainter;
    private final CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();

    private Consumer<GeoPosition> clickListener = null;

    private double zoomLevelDouble = 0.0;
    private double panOffsetX = 0.0;
    private double panOffsetY = 0.0;

    private double lastWheelRotation = 0.0;
    private long lastWheelTime = 0L;
    private int consecutiveSmallRotations = 0;
    private int consecutiveSameDirection = 0;
    private double totalScrollMagnitude = 0.0;

    private double pendingHorizontalScroll = 0.0;
    private long lastScrollEventTime = 0L;
    private long pinchSessionExpiry = 0L;

    public MapPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        System.setProperty("http.agent", "MyMapApp/1.0 (contact@example.com)");

        HttpsOsmTileFactoryInfo info = new HttpsOsmTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setAddressLocation(new GeoPosition(43.6532, -79.3832));
        mapViewer.setZoom(5);

        for (MouseWheelListener listener : mapViewer.getMouseWheelListeners()) {
            mapViewer.removeMouseWheelListener(listener);
        }

        zoomLevelDouble = mapViewer.getZoom();

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseWheelEvent) {
                MouseWheelEvent mwe = (MouseWheelEvent) event;
                if (mwe.getComponent() == mapViewer || SwingUtilities.isDescendingFrom(mwe.getComponent(), mapViewer)) {
                    try {
                        java.lang.reflect.Field field = mwe.getClass().getDeclaredField("isHorizontal");
                        field.setAccessible(true);
                        Boolean isHorizontal = (Boolean) field.get(mwe);

                        if (Boolean.TRUE.equals(isHorizontal)) {
                            pendingHorizontalScroll = mwe.getPreciseWheelRotation();
                            lastScrollEventTime = System.currentTimeMillis();
                        } else {
                            if (System.currentTimeMillis() - lastScrollEventTime > 50) {
                                pendingHorizontalScroll = 0.0;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);

        mapViewer.addMouseWheelListener(e -> {
            try {
                double precise = e.getPreciseWheelRotation();
                int wheelRot = e.getWheelRotation();
                if (precise == 0.0 && wheelRot == 0) return;

                long now = System.currentTimeMillis();

                boolean isPinchZoom = false;

                long timeDelta = now - lastWheelTime;

                if (timeDelta > 200) {
                    consecutiveSmallRotations = 0;
                    consecutiveSameDirection = 0;
                    totalScrollMagnitude = 0.0;
                    pinchSessionExpiry = 0L;
                }

                boolean isDirectionalScroll = false;
                if (timeDelta < 100 && Math.abs(precise) > 0.001) {
                    if (Math.signum(precise) == Math.signum(lastWheelRotation)) {
                        consecutiveSameDirection++;
                        totalScrollMagnitude += Math.abs(precise);

                        if (consecutiveSameDirection >= 2) {
                            isDirectionalScroll = true;
                        }
                    } else {
                        consecutiveSameDirection = 0;
                        totalScrollMagnitude = Math.abs(precise);
                    }
                }

                if (isDirectionalScroll) {
                    isPinchZoom = false;
                    consecutiveSmallRotations = 0; // Reset pinch counter
                }
                else if (timeDelta < PINCH_TIME_WINDOW_MS * 2 && Math.abs(precise) > 0.001) {
                    boolean alternatingDirection = Math.signum(precise) != Math.signum(lastWheelRotation)
                            && Math.abs(lastWheelRotation) > 0.0;
                    boolean withinPinchBounds = Math.abs(precise) <= PINCH_ROTATION_THRESHOLD && timeDelta <= PINCH_TIME_WINDOW_MS;

                    if (alternatingDirection && withinPinchBounds) {
                        consecutiveSmallRotations = Math.min(consecutiveSmallRotations + 1, PINCH_ACTIVATION_COUNT + 2);
                        if (consecutiveSmallRotations >= PINCH_ACTIVATION_COUNT) {
                            isPinchZoom = true;
                            pinchSessionExpiry = now + PINCH_SESSION_TIMEOUT_MS;
                        }
                    } else if (timeDelta > PINCH_TIME_WINDOW_MS * 2 || Math.abs(precise) > PINCH_ROTATION_THRESHOLD * 2) {
                        consecutiveSmallRotations = Math.max(0, consecutiveSmallRotations - 1);
                    }
                } else {
                    consecutiveSmallRotations = 0;
                }

                if (!isPinchZoom && pinchSessionExpiry > now) {
                    isPinchZoom = true;
                }

                lastWheelRotation = precise;
                lastWheelTime = now;

                double rotationDiff = Math.abs(Math.abs(precise) - Math.abs(wheelRot));
                boolean precisionSuggestsTrackpad = rotationDiff > 0.001 || Math.abs(precise) < 0.75;
                boolean frequencySuggestsTrackpad = timeDelta > 0 && timeDelta < 35 && Math.abs(precise) <= 1.5;
                boolean zeroRotationButPrecise = Math.abs(wheelRot) == 0 && Math.abs(precise) > 0.0;
                boolean isLikelyTrackpad = precisionSuggestsTrackpad || frequencySuggestsTrackpad || zeroRotationButPrecise;
                boolean isHardwareWheel = !isLikelyTrackpad && Math.abs(wheelRot) >= 1;

                boolean shouldZoom = e.isControlDown() || isPinchZoom || isHardwareWheel;

                if (shouldZoom) {
                    double deltaUnits;
                    if (isHardwareWheel) {
                        deltaUnits = wheelRot * HARDWARE_WHEEL_STEP;
                        panOffsetX = 0.0;
                        panOffsetY = 0.0;
                        pendingHorizontalScroll = 0.0;
                    } else {
                        double perStep = precise * TRACKPAD_PINCH_STEP;
                        deltaUnits = perStep * PINCH_STEPS_PER_EVENT;
                    }

                    zoomLevelDouble += deltaUnits;
                    zoomLevelDouble = Math.max(0.0, Math.min(MAX_Z, zoomLevelDouble));

                    int targetInt = (int) Math.round(zoomLevelDouble);
                    if (targetInt != mapViewer.getZoom()) {
                        Point mousePos = e.getPoint();
                        GeoPosition geoAtMouse = mapViewer.convertPointToGeoPosition(mousePos);

                        if (geoAtMouse != null) {
                            mapViewer.setZoom(targetInt);
                            java.awt.geom.Point2D newMouseScreenPos2D = mapViewer.convertGeoPositionToPoint(geoAtMouse);

                            if (newMouseScreenPos2D != null) {
                                Point newMouseScreenPos = new Point((int) newMouseScreenPos2D.getX(), (int) newMouseScreenPos2D.getY());
                                int dx = mousePos.x - newMouseScreenPos.x;
                                int dy = mousePos.y - newMouseScreenPos.y;


                                Point centerP = new Point(mapViewer.getWidth() / 2, mapViewer.getHeight() / 2);
                                int targetX = centerP.x + dx;
                                int targetY = centerP.y + dy;

                                GeoPosition newCenter = mapViewer.convertPointToGeoPosition(new Point(targetX, targetY));
                                if (newCenter != null) {
                                    mapViewer.setAddressLocation(newCenter);
                                }
                            }
                        }
                    }
                } else if (!isHardwareWheel) {

                    final double PAN_SENSITIVITY = 35.0;

                    double scrollDeltaX = 0.0;
                    double scrollDeltaY = 0.0;

                    long now2 = System.currentTimeMillis();
                    if (now2 - lastScrollEventTime < 100 && Math.abs(pendingHorizontalScroll) > 0.001) {
                        scrollDeltaX = pendingHorizontalScroll * PAN_SENSITIVITY;
                        scrollDeltaY = 0.0;
                        pendingHorizontalScroll = 0.0;
                    } else if (e.isShiftDown()) {
                        scrollDeltaX = precise * PAN_SENSITIVITY;
                        scrollDeltaY = 0.0;
                    } else {
                        scrollDeltaX = 0.0;
                        scrollDeltaY = precise * PAN_SENSITIVITY;
                    }

                    panOffsetX += scrollDeltaX;
                    panOffsetY += scrollDeltaY;

                    final double APPLY_THRESH = 1.0;
                    if (Math.abs(panOffsetX) >= APPLY_THRESH || Math.abs(panOffsetY) >= APPLY_THRESH) {
                        Point centerP = new Point(mapViewer.getWidth()/2, mapViewer.getHeight()/2);
                        int targetX = (int) Math.round(centerP.x + panOffsetX);
                        int targetY = (int) Math.round(centerP.y + panOffsetY);

                        GeoPosition gp = mapViewer.convertPointToGeoPosition(new Point(targetX, targetY));
                        if (gp != null) {
                            mapViewer.setAddressLocation(gp);
                        }

                        panOffsetX -= (targetX - centerP.x);
                        panOffsetY -= (targetY - centerP.y);
                    }
                }

                e.consume();
            } catch (Exception ignored) {}
        });

        waypointPainter.setWaypoints(waypoints);
        routePainter = new RoutePainter(null);
        numberedMarkerPainter = new NumberedMarkerPainter(markerPositions);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter, numberedMarkerPainter));
        mapViewer.setOverlayPainter(compoundPainter);

        add(mapViewer, BorderLayout.CENTER);

        enableDragging();

        enableClickToAddMarker();
    }

    private void enableDragging() {
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);

        // Add custom listener for middle-click dragging
        MouseInputListener middleClickListener = new MiddleClickPanListener(mapViewer);
        mapViewer.addMouseListener(middleClickListener);
        mapViewer.addMouseMotionListener(middleClickListener);
    }

    /**
     * Custom mouse input listener to handle middle-click (scroll wheel) dragging
     * for panning the map, matching left-click drag behavior.
     */
    private static class MiddleClickPanListener implements MouseInputListener {
        private final JXMapViewer mapViewer;
        private Point lastPoint = null;
        private boolean isDragging = false;

        public MiddleClickPanListener(JXMapViewer mapViewer) {
            this.mapViewer = mapViewer;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                isDragging = true;
                lastPoint = e.getPoint();
                e.consume();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                isDragging = false;
                lastPoint = null;
                e.consume();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (isDragging && lastPoint != null) {
                int dx = e.getX() - lastPoint.x;
                int dy = e.getY() - lastPoint.y;

                Point centerP = new Point(mapViewer.getWidth() / 2, mapViewer.getHeight() / 2);
                int targetX = centerP.x - dx;
                int targetY = centerP.y - dy;

                GeoPosition gp = mapViewer.convertPointToGeoPosition(new Point(targetX, targetY));
                if (gp != null) {
                    mapViewer.setAddressLocation(gp);
                }

                lastPoint = e.getPoint();
                e.consume();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // Not needed for middle-click panning
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Not needed
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Not needed
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // Not needed
        }
    }


    private void enableClickToAddMarker() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GeoPosition gp = mapViewer.convertPointToGeoPosition(e.getPoint());
                if (clickListener != null) {
                    clickListener.accept(gp);
                } else {
                    addMarker(gp);
                }
            }
        });
    }


    public void setCenter(double latitude, double longitude) {
        mapViewer.setAddressLocation(new GeoPosition(latitude, longitude));
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }


    private void addMarker(GeoPosition position) {
        markerPositions.add(position);
        waypoints.clear();
        for (GeoPosition gp : markerPositions) {
            waypoints.add(new DefaultWaypoint(gp));
        }
        waypointPainter.setWaypoints(waypoints);
        numberedMarkerPainter.setPositions(markerPositions);
        mapViewer.repaint();
    }

    public void addStop(double latitude, double longitude) {
        GeoPosition gp = new GeoPosition(latitude, longitude);
        markerPositions.add(gp);
        waypoints.clear();
        for (GeoPosition p : markerPositions) {
            waypoints.add(new DefaultWaypoint(p));
        }
        waypointPainter.setWaypoints(waypoints);
        numberedMarkerPainter.setPositions(markerPositions);
        mapViewer.repaint();
    }

    public void setStops(List<GeoPosition> positions) {
        markerPositions.clear();
        if (positions != null) markerPositions.addAll(positions);
        waypoints.clear();
        for (GeoPosition p : markerPositions) {
            waypoints.add(new DefaultWaypoint(p));
        }
        waypointPainter.setWaypoints(waypoints);
        numberedMarkerPainter.setPositions(markerPositions);
        mapViewer.repaint();
    }

    public void clearStops() {
        markerPositions.clear();
        waypoints.clear();
        waypointPainter.setWaypoints(waypoints);
        numberedMarkerPainter.setPositions(markerPositions);
        clearRoute();
        mapViewer.repaint();
    }

    public List<GeoPosition> getLastTwoMarkerPositions() {
        int size = markerPositions.size();
        if (size < 2) return new ArrayList<>();
        GeoPosition a = markerPositions.get(size - 2);
        GeoPosition b = markerPositions.get(size - 1);
        return Arrays.asList(a, b);
    }

    public void setRoute(List<GeoPosition> route) {
        this.routePainter = new RoutePainter(route);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter, numberedMarkerPainter));
        mapViewer.repaint();
    }

    public void setRouteSegments(List<List<GeoPosition>> segments) {
        this.routePainter = new RoutePainter(null);
        this.routePainter.setSegments(segments);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter, numberedMarkerPainter));
        mapViewer.repaint();
    }

    public void clearRoute() {
        setRoute(null);
    }

    public void setClickListener(Consumer<GeoPosition> listener) {
        this.clickListener = listener;
    }

    public static class HttpsOsmTileFactoryInfo extends OSMTileFactoryInfo {
        public HttpsOsmTileFactoryInfo() {
            super("OpenStreetMap HTTPS",
                    "https://tile.openstreetmap.org"
            );
        }
    }
}
