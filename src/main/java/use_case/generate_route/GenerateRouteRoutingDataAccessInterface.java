package use_case.generate_route;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public interface GenerateRouteRoutingDataAccessInterface {
    List<GeoPosition> getRoute(GeoPosition start, GeoPosition end, String profile) throws Exception;
}
