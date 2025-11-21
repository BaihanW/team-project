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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.function.Consumer;


public class MapPanel extends JPanel {

    private final JXMapViewer mapViewer;

    private final List<GeoPosition> markerPositions = new ArrayList<>();

    private final Set<Waypoint> waypoints = new HashSet<>();

    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    private RoutePainter routePainter;
    private NumberedMarkerPainter numberedMarkerPainter;
    private final CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();

    private Consumer<GeoPosition> clickListener = null;

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

        // Add mouse wheel zoom: forward (wheel up/negative rotation) -> zoom in; backward -> zoom out
        mapViewer.addMouseWheelListener(e -> {
            try {
                int notches = e.getWheelRotation(); // negative = wheel up/forward, positive = wheel down/backward
                if (notches == 0) return;
                int z = mapViewer.getZoom();
                int newZ = z;
                final int MAX_Z = 20;
                if (notches < 0) {
                    // wheel moved up/forward -> zoom in (decrease zoom index)
                    newZ = Math.max(0, z - Math.abs(notches));
                } else {
                    // wheel moved down/backward -> zoom out (increase zoom index)
                    newZ = Math.min(MAX_Z, z + notches);
                }
                if (newZ != z) {
                    // preserve current center (use visible center)
                    GeoPosition center = mapViewer.convertPointToGeoPosition(new Point(mapViewer.getWidth()/2, mapViewer.getHeight()/2));
                    mapViewer.setZoom(newZ);
                    mapViewer.setAddressLocation(center);
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
