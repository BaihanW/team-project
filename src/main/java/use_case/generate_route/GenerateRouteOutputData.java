package use_case.generate_route;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class GenerateRouteOutputData {
    private final List<List<GeoPosition>> segments;

    public GenerateRouteOutputData(List<List<GeoPosition>> segments) {
        this.segments = segments;
    }

    public List<List<GeoPosition>> getSegments() {
        return segments;
    }
}
