package data_access;

import entity.Location;
import entity.Marker;
import use_case.add_marker.AddMarkerDataAccessInterface;

import java.util.ArrayList;
import  java.util.List;

public class InMemoryMarkerDataAccessObject implements AddMarkerDataAccessInterface {

    private final List<Marker> markers = new ArrayList<>();

    @Override
    public boolean exists(Location location) {
        for (Marker m: markers) {
            if (m.getLatitude() ==  location.getLatitude() && m.getLongitude() == location.getLongitude()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void save(Marker marker) {
        markers.add(marker);
    }

    @Override
    public List<Marker> getAllMarkers() {
        return new ArrayList<>(markers);
    }
}

