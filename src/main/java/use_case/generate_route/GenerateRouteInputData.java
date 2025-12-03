package use_case.generate_route;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class GenerateRouteInputData {
    private final String profile;
    private final List<GeoPosition> stops;

    public GenerateRouteInputData(String profile, List<GeoPosition> stops) {
        this.profile = profile;
        this.stops = stops;
    }

    public String getProfile() {
        return profile;
    }

    public List<GeoPosition> getStops() {
        return stops;
    }
}
