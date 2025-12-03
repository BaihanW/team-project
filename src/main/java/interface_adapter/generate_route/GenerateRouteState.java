package interface_adapter.generate_route;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class GenerateRouteState {
    private List<List<GeoPosition>> routeSegments = new ArrayList<>();
    private String errorMessage;

    public GenerateRouteState() {}

    public GenerateRouteState(GenerateRouteState copy) {
        this.routeSegments = new ArrayList<>();
        if (copy != null) {
            for (List<GeoPosition> segment : copy.getRouteSegments()) {
                this.routeSegments.add(new ArrayList<>(segment));
            }
            this.errorMessage = copy.getErrorMessage();
        }
    }

    public List<List<GeoPosition>> getRouteSegments() {
        List<List<GeoPosition>> copy = new ArrayList<>();
        for (List<GeoPosition> segment : routeSegments) {
            copy.add(new ArrayList<>(segment));
        }
        return copy;
    }

    public void setRouteSegments(List<List<GeoPosition>> routeSegments) {
        List<List<GeoPosition>> copy = new ArrayList<>();
        for (List<GeoPosition> segment : routeSegments) {
            copy.add(new ArrayList<>(segment));
        }
        this.routeSegments = copy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}