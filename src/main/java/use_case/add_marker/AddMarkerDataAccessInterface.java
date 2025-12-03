package use_case.add_marker;

import entity.Location;
import entity.Marker;

import java.util.List;

public interface AddMarkerDataAccessInterface {

    boolean exists(Location location);
    void save(Marker marker);

    List<Marker> getAllMarkers();

}