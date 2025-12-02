package view;

import org.jxmapviewer.*;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.painter.CompoundPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * MapPanel
 * Responsibilities:
 *  - Display OpenStreetMap using JXMapViewer.
 *  - Smooth zooming (mouse wheel, trackpad pinch).
 *  - Smooth panning (two-finger scroll).
 */
public class MapPanel extends JPanel {

    /* ================================================================
     *                   CONFIGURATION CONSTANTS
     * ================================================================ */

    private static final int MAX_ZOOM = 20;

    // Trackpad zoom characteristics
    private static final double PINCH_STEP = 0.03;
    private static final int PINCH_MULTIPLIER = 20;

    // Mouse wheel zoom characteristics
    private static final double MOUSE_WHEEL_STEP = 1.2;

    // Pinch gesture recognition thresholds
    private static final long PINCH_TIMEOUT_MS = 180;
    private static final double PINCH_ROTATION_THRESHOLD = 0.12;

    /* ================================================================
     *                   INSTANCE FIELDS
     * ================================================================ */

    /** The JXMapViewer instance that renders the OSM map. */
    private final JXMapViewer mapViewer;

    private final List<GeoPosition> markerPositions = new ArrayList<>();

    private final Set<Waypoint> waypoints = new HashSet<>();

    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    private RoutePainter routePainter;
    private NumberedMarkerPainter numberedMarkerPainter;
    private final CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();

    private Consumer<GeoPosition> clickListener = null;

    /* ------------------- Smooth Zoom & Pan State -------------------- */

    /** Fractional zoom accumulator (for ultra-smooth trackpad zooming). */
    private double smoothZoom = 0;

    /** Accumulated pan delta (used before applying to map center). */
    private double panOffsetX = 0;
    private double panOffsetY = 0;

    /* Trackpad pinch detection fields */
    private double lastRotation = 0;
    private long lastRotationTime = 0;
    private int alternatingSmallRotations = 0;
    private int sameDirectionCount = 0;
    private long pinchSessionExpire = 0;

    /* Trackpad horizontal scrolling detection */
    private long lastScrollEventTime = 0;

    /* ================================================================
     *                    CONSTRUCTOR
     * ================================================================ */

    public MapPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // Required for OSM to accept tile requests
        System.setProperty("http.agent", "TripPlanner/1.0");

        // Configure HTTPS tile factory for OSM
        DefaultTileFactory tileFactory =
                new DefaultTileFactory(new HttpsOsmTileFactoryInfo());

        // Initialize map viewer
        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        // Default position = Toronto
        mapViewer.setAddressLocation(new GeoPosition(43.6532, -79.3832));
        mapViewer.setZoom(5);

        // Remove default wheel zoom
        removeDefaultWheelListeners();

        // Initialize smooth zoom
        JPanel zoomControlsContainer = buildZoomControls();
        add(zoomControlsContainer, BorderLayout.NORTH);
        smoothZoom = mapViewer.getZoom();

        // Install refined trackpad horizontal-scroll detector
        installHorizontalScrollProbe();

        // Install main wheel handler (zoom + pan)
        installSmoothWheelHandler();

        // Drag-to-pan support
        enableDragPanning();

        waypointPainter.setWaypoints(waypoints);
        routePainter = new RoutePainter(null);
        numberedMarkerPainter = new NumberedMarkerPainter(markerPositions);
        compoundPainter.setPainters(Arrays.asList(routePainter, waypointPainter, numberedMarkerPainter));
        mapViewer.setOverlayPainter(compoundPainter);

        add(mapViewer, BorderLayout.CENTER);

        enableClickToAddMarker();
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

    /* ================================================================
     *                    INITIALIZATION HELPERS
     * ================================================================ */

    /** Removes default JXMapViewer mouse wheel behavior. */
    private void removeDefaultWheelListeners() {
        for (MouseWheelListener listener : mapViewer.getMouseWheelListeners()) {
            mapViewer.removeMouseWheelListener(listener);
        }
    }

    /**
     * Installs a global AWT listener to detect horizontal scroll values.
     * Some platforms send separate horizontal wheel events,
     * but JXMapViewer does not expose them directly.
     */
    private void installHorizontalScrollProbe() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof MouseWheelEvent mwe)) return;
            if (!SwingUtilities.isDescendingFrom(mwe.getComponent(), mapViewer)) return;

            try {
                // Some JVMs store a private field "isHorizontal"
                var field = mwe.getClass().getDeclaredField("isHorizontal");
                boolean isHorizontal = (Boolean) field.get(mwe);

                if (isHorizontal) {
                    lastScrollEventTime = System.currentTimeMillis();
                }
            } catch (Exception ignored) {
                // On platforms/jvms without horizontal field, ignore.
            }
        }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    /**
     * Build the zoom control panel (top-right).
     */
    private JPanel buildZoomControls() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        container.setOpaque(false);

        JButton zoomIn = buildZoomButton("+", "Zoom in", -1);
        JButton zoomOut = buildZoomButton("-", "Zoom out", +1);

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        inner.setOpaque(false);
        inner.add(zoomIn);
        inner.add(zoomOut);

        container.add(inner);
        container.setPreferredSize(new Dimension(0, 44));

        return container;
    }

    /**
     * Create a single zoom button with its behavior encapsulated.
     */
    private JButton buildZoomButton(String label, String tooltip, int zoomDelta) {
        JButton btn = new JButton(label);
        btn.setPreferredSize(new Dimension(36, 24));
        btn.setToolTipText(tooltip);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setMargin(new Insets(2, 4, 2, 4));

        btn.addActionListener(e -> {
            try {
                JComponent viewer = this.getMapViewer();
                GeoPosition center = this.getMapViewer()
                        .convertPointToGeoPosition(new Point(viewer.getWidth() / 2, viewer.getHeight() / 2));
                int z = this.getMapViewer().getZoom();
                int newZ = Math.min(20, Math.max(0, z + zoomDelta));
                this.getMapViewer().setZoom(newZ);
                this.getMapViewer().setCenterPosition(center);
            } catch (Exception ignored) {}
        });

        return btn;
    }

    /**
     * Attach the main wheel listener that handles:
     * - Smooth trackpad pinch zoom
     * - Smooth two-finger panning
     * - Hardware mouse wheel zooming
     */
    private void installSmoothWheelHandler() {
        mapViewer.addMouseWheelListener(e -> {
            try {
                handleWheelEvent(e);
                e.consume();
            } catch (Exception ignored) {}
        });
    }

    /** Enables click-and-drag panning. */
    private void enableDragPanning() {
        MouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
    }

    /* ================================================================
     *                    WHEEL HANDLING (zoom + pan)
     * ================================================================ */

    /**
     * Handles all zoom/pan logic for mouse wheel and trackpad gestures.
     * (Implementation continues below in next message.)
     */
    private void handleWheelEvent(MouseWheelEvent e) {

        double preciseRot = e.getPreciseWheelRotation();  // fractional (trackpads)
        int wheelNotches = e.getWheelRotation();          // integer (hardware wheel)
        long now = System.currentTimeMillis();

        if (preciseRot == 0.0 && wheelNotches == 0) return;

        long dt = now - lastRotationTime;
        boolean quickEvent = dt < 100;


        /* --------------------------------------------------------------
         *  Detect whether this sequence is a directional scroll
         *  (trackpad vertical/horizontal sliding), not a pinch gesture.
         *
         *  Continuous same-direction movements → scrolling.
         *  Alternating small movements → pinch.
         * -------------------------------------------------------------- */
        boolean directionalScroll = false;

        if (quickEvent && Math.abs(preciseRot) > 0.001) {
            if (Math.signum(preciseRot) == Math.signum(lastRotation)) {
                sameDirectionCount++;

                // Even 2 same-direction events in a row implies "scrolling".
                if (sameDirectionCount >= 2) {
                    directionalScroll = true;
                }
            } else {
                sameDirectionCount = 0;  // direction flipped
            }
        }


        /* --------------------------------------------------------------
         *  Pinch gesture detection:
         *      - consecutive alternating small rotations
         *      - short time window
         *      - very small rotation magnitude
         * -------------------------------------------------------------- */
        boolean isPinchGesture = false;

        if (!directionalScroll) {

            boolean alternating =
                    Math.signum(preciseRot) != Math.signum(lastRotation)
                            && Math.abs(lastRotation) > 0.0;

            boolean smallEnough =
                    Math.abs(preciseRot) <= PINCH_ROTATION_THRESHOLD;

            if (quickEvent && alternating && smallEnough) {
                alternatingSmallRotations++;
                if (alternatingSmallRotations >= 1) {  // threshold = 1 for responsiveness
                    isPinchGesture = true;
                    pinchSessionExpire = now + PINCH_TIMEOUT_MS;
                }
            }
        }

        // If inside an active pinch session, keep interpreting as pinch
        if (!isPinchGesture && pinchSessionExpire > now) {
            isPinchGesture = true;
        }

        lastRotation = preciseRot;
        lastRotationTime = now;


        /* --------------------------------------------------------------
         *  Distinguish "hardware wheel" from "trackpad"
         *  (This must happen *after* pinch detection)
         * -------------------------------------------------------------- */
        boolean looksLikeTrackpad =
                Math.abs(preciseRot - wheelNotches) > 0.001
                        || Math.abs(preciseRot) < 0.75;

        boolean couldBeTrackpad =
                looksLikeTrackpad
                        || (dt > 0 && dt < 35);   // fast gesture frequency

        boolean isHardwareWheel =
                !couldBeTrackpad
                        && Math.abs(wheelNotches) >= 1;


        /* --------------------------------------------------------------
         *  Determine whether this event should trigger ZOOM
         * -------------------------------------------------------------- */
        boolean zoomIntent =
                e.isControlDown()      // ctrl+scroll → zoom
                        || isPinchGesture      // trackpad pinch → zoom
                        || isHardwareWheel;    // mouse wheel → zoom


        /* ==============================================================
         *  ZOOM
         * ==============================================================*/
        AtomicInteger pendingHorizontal = new AtomicInteger();
        if (zoomIntent) {

            double zoomDelta;

            if (isHardwareWheel) {
                // Hardware mouse wheel → coarse zoom
                zoomDelta = wheelNotches * MOUSE_WHEEL_STEP;

                // Hardware wheel must NOT pan — clear pending pan state.
                panOffsetX = 0;
                panOffsetY = 0;
                pendingHorizontal.set(0);

            } else {
                // Trackpad pinch → extremely smooth zooming
                double perStep = preciseRot * PINCH_STEP;
                zoomDelta = perStep * PINCH_MULTIPLIER;
            }

            // Apply smooth zoom accumulator
            smoothZoom += zoomDelta;
            smoothZoom = Math.max(0.0, Math.min(MAX_ZOOM, smoothZoom));

            int newZoom = (int) Math.round(smoothZoom);

            if (newZoom != mapViewer.getZoom()) {
                GeoPosition center = mapViewer.getAddressLocation();
                mapViewer.setZoom(newZoom);
                mapViewer.setAddressLocation(center);
            }

            return;
        }


        /* ==============================================================
         *  PAN HANDLING (trackpad two-finger scrolling)
         * ==============================================================*/

        final double PAN_SENSITIVITY = 35.0;

        double dx = 0;
        double dy = 0;

        long now2 = System.currentTimeMillis();

        // If we recently captured horizontal scroll info, use it
        if (now2 - lastScrollEventTime < 100
                && Math.abs(pendingHorizontal.get()) > 0.001) {

            dx = pendingHorizontal.get() * PAN_SENSITIVITY;
            dy = 0;
            pendingHorizontal.set(0);  // consume

        } else if (e.isShiftDown()) {
            // Shift + vertical scroll → horizontal pan (fallback)
            dx = preciseRot * PAN_SENSITIVITY;
            dy = 0;

        } else {
            // Standard vertical two-finger pan
            dx = 0;
            dy = preciseRot * PAN_SENSITIVITY;
        }

        // Accumulate pan offsets for smooth multi-event movement
        panOffsetX += dx;
        panOffsetY += dy;

        final double APPLY_THRESHOLD = 1.0;

        if (Math.abs(panOffsetX) >= APPLY_THRESHOLD ||
                Math.abs(panOffsetY) >= APPLY_THRESHOLD) {

            // Convert pixel offset into new map center
            Point centerPoint = new Point(
                    mapViewer.getWidth() / 2,
                    mapViewer.getHeight() / 2
            );

            int targetX = (int) Math.round(centerPoint.x + panOffsetX);
            int targetY = (int) Math.round(centerPoint.y + panOffsetY);

            GeoPosition gp = mapViewer.convertPointToGeoPosition(
                    new Point(targetX, targetY)
            );

            if (gp != null) {
                mapViewer.setAddressLocation(gp);
            }

            // Remove applied movement from offset
            panOffsetX -= (targetX - centerPoint.x);
            panOffsetY -= (targetY - centerPoint.y);
        }
    }


    /* ================================================================
     *               Repaints (used by SearchView)
     * ================================================================ */

    /** Centers the map on the given coordinates. */
    public void setCenter(double lat, double lon) {
        mapViewer.setAddressLocation(new GeoPosition(lat, lon));
        mapViewer.repaint();
    }

    public JXMapViewer getMapViewer() { return mapViewer; }

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

    /* ================================================================
     *             Custom HTTPS OSM Tile Loader
     * ================================================================ */

    public static class HttpsOsmTileFactoryInfo extends OSMTileFactoryInfo {
        public HttpsOsmTileFactoryInfo() {
            super("OpenStreetMap-HTTPS", "https://tile.openstreetmap.org");
        }
    }
}
