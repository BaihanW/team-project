package view;

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

/**
 * View for the Add Marker (pin) Use Case.
 *
 * Responsibilities (UI only):
 * - Show a map
 * - Let the user click to request "add marker" via the controller
 * - Listen to AddMarkerViewModel changes and update the pins (waypoints)
 *
 * ❗ 이 View는 엔티티( Location, Marker )에 직접 의존하지 않고,
 *    오직 ViewModel 상태(위도, 경도 primitive 값)만 사용한다.
 */
public class AddMarkerView extends JPanel implements PropertyChangeListener {

    private final String viewName = "add marker";

    private final JXMapViewer mapViewer;
    private final AddMarkerController addMarkerController;
    private final AddMarkerViewModel addMarkerViewModel;

    private final Set<Waypoint> waypoints;
    private final WaypointPainter<Waypoint> waypointPainter;

    /**
     * Constructs the AddMarkerView.
     *
     * @param mapViewer           the JXMapViewer instance used to display the map
     * @param addMarkerController the controller for the Add Marker Use Case
     * @param addMarkerViewModel  the ViewModel for the Add Marker Use Case
     */
    public AddMarkerView(JXMapViewer mapViewer,
                         AddMarkerController addMarkerController,
                         AddMarkerViewModel addMarkerViewModel) {
        this.mapViewer = mapViewer;
        this.addMarkerController = addMarkerController;
        this.addMarkerViewModel = addMarkerViewModel;

        this.waypoints = new HashSet<>();
        this.waypointPainter = new WaypointPainter<>();

        // Configure painter
        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);

        // Listen for state changes from the ViewModel
        this.addMarkerViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        add(mapViewer, BorderLayout.CENTER);

        setupMouseListener();
    }

    /**
     * @return the name of this view, if you use a ViewManager.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Reacts to changes in the AddMarkerViewModel's state.
     * ViewModel → View 업데이트만 담당하고 엔티티는 모른다.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) {
            return;
        }

        AddMarkerState state = addMarkerViewModel.getState();

        // Handle error
        if (state.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Add Marker Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Handle new marker (primitive lat/lon만 사용)
        if (state.getLastMarkerLatitude() != null &&
                state.getLastMarkerLongitude() != null) {

            double lat = state.getLastMarkerLatitude();
            double lon = state.getLastMarkerLongitude();

            addMarker(lat, lon);
        }
    }

    /**
     * Adds a marker (pin) to the map and repaints.
     * 엔티티 대신 위도/경도 primitive 값만 사용.
     */
    private void addMarker(double latitude, double longitude) {
        GeoPosition gp = new GeoPosition(latitude, longitude);

        waypoints.add(new DefaultWaypoint(gp));
        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    /**
     * Sets up mouse listening so that clicking on the map
     * triggers the Add Marker Use Case via the controller.
     */
    private void setupMouseListener() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GeoPosition gp = mapViewer.convertPointToGeoPosition(e.getPoint());
                // View는 단순히 좌표만 전달하고, 비즈니스 로직은 Controller/Interactor에 맡김
                addMarkerController.addMarker(gp.getLatitude(), gp.getLongitude());
            }
        });
    }
}
