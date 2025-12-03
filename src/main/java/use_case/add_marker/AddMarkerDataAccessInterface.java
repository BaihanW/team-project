package use_case.add_marker;

import entity.Marker;
import java.util.List;

public interface AddMarkerDataAccessInterface {
    void save(Marker marker);
    List<Marker> getAllMarkers();
}