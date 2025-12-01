package use_case.add_marker;

import entity.Location;
import entity.Marker;

import java.util.List;

/**
 * Interactor가 의존하는 Data Access 인터페이스.
 * - 엔티티 타입(Marker, Location)을 써도 됨 (엔티티는 더 안쪽 레이어니까 OK)
 */
public interface AddMarkerDataAccessInterface {

    /** 새 마커를 저장한다. (중복 체크는 Interactor 쪽에서 함) */
    void save(Marker marker);

    /** 같은 위치에 이미 마커가 있는지 확인한다. */
    boolean exists(Location location);

    /** 현재 저장된 모든 마커를 반환한다. */
    List<Marker> allMarkers();
}
