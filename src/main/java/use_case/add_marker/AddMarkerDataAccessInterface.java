package use_case.add_marker;

import entity.Location;
import entity.Marker;

import java.util.List;

/**
 * Interactor가 의존하는 Data Access 인터페이스.
 * - 엔티티 타입(Marker, Location)을 써도 됨 (엔티티는 더 안쪽 레이어니까 OK)
 */
public interface AddMarkerDataAccessInterface {

    /** Save new markers. Already Existing markers are checked by an interactor */
    void save(Marker marker);


    boolean exists(Location location);


    List<Marker> allMarkers();
}
