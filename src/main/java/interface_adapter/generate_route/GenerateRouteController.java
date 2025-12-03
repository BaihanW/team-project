package interface_adapter.generate_route;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.generate_route.GenerateRouteInputBoundary;
import use_case.generate_route.GenerateRouteInputData;

import java.util.List;

public class GenerateRouteController {
    private final GenerateRouteInputBoundary generateRouteInputBoundary;

    public GenerateRouteController(GenerateRouteInputBoundary generateRouteInputBoundary) {
        this.generateRouteInputBoundary = generateRouteInputBoundary;
    }

    public void generate(String profile, List<GeoPosition> stops) {
        generateRouteInputBoundary.execute(new GenerateRouteInputData(profile, stops));
    }
}
