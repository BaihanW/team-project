package view;

import entity.Marker;
import entity.Location;
import interface_adapter.addMarker.AddMarkerController;
import interface_adapter.addMarker.AddMarkerState;
import interface_adapter.addMarker.AddMarkerViewModel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

public class AddMarkerView extends JPanel implements PropertyChangeListener {

    private final String viewName = "add marker";

    private final JXMapViewer mapViewer;
    private final AddMarkerController addMarkerController;
    private final AddMarkerViewModel addMarkerViewModel;

    private final Set<Waypoint> waypoints;
    private final WaypointPainter<Waypoint> waypointPainter;

    public AddMarkerView(JXMapViewer mapViewer,
                         AddMarkerController addMarkerController,
                         AddMarkerViewModel addMarkerViewModel) {
        this.mapViewer = mapViewer;
        this.addMarkerController = addMarkerController;
        this.addMarkerViewModel = addMarkerViewModel;

        this.waypoints = new HashSet<>();
        this.waypointPainter = new WaypointPainter<>();

        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);

        this.addMarkerViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        add(mapViewer, BorderLayout.CENTER);

        setupMouseListener();
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) {
            return;
        }

        AddMarkerState state = addMarkerViewModel.getState();

        if (state.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Add Marker Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (state.getLastMarkerLatitude() != null &&
                state.getLastMarkerLongitude() != null) {

            double lat = state.getLastMarkerLatitude();
            double lon = state.getLastMarkerLongitude();

            Location location = new Location("", lat, lon);
            Marker marker = new Marker(location);

            addMarker(marker);
        }
    }

    private void addMarker(Marker marker) {
        GeoPosition gp = new GeoPosition(
                marker.getLatitude(),
                marker.getLongitude()
        );

        waypoints.add(new DefaultWaypoint(gp));
        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    private void setupMouseListener() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GeoPosition gp = mapViewer.convertPointToGeoPosition(e.getPoint());
                addMarkerController.addMarker(gp.getLatitude(), gp.getLongitude());
            }
        });
    }
}

