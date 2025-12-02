package use_case.generate_route;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class GenerateRouteInteractor implements GenerateRouteInputBoundary {
    private final GenerateRouteRoutingDataAccessInterface routingDataAccessInterface;
    private final GenerateRouteOutputBoundary generateRoutePresenter;

    public GenerateRouteInteractor(GenerateRouteRoutingDataAccessInterface routingDataAccessInterface,
                                   GenerateRouteOutputBoundary generateRoutePresenter) {
        this.routingDataAccessInterface = routingDataAccessInterface;
        this.generateRoutePresenter = generateRoutePresenter;
    }

    @Override
    public void execute(GenerateRouteInputData inputData) {
        List<GeoPosition> stops = inputData.getStops();
        if (stops.size() < 2) {
            generateRoutePresenter.prepareFailView("Add at least two stops to compute a full route.");
            return;
        }

        String profile = inputData.getProfile();
        List<List<GeoPosition>> segments = new ArrayList<>();
        for (int i = 0; i < stops.size() - 1; i++) {
            GeoPosition a = stops.get(i);
            GeoPosition b = stops.get(i + 1);
            try {
                List<GeoPosition> segment = routingDataAccessInterface.getRoute(a, b, profile);
                if (segment != null && !segment.isEmpty()) {
                    segments.add(segment);
                } else {
                    List<GeoPosition> straight = new ArrayList<>();
                    straight.add(a);
                    straight.add(b);
                    segments.add(straight);
                }
            } catch (Exception e) {
                List<GeoPosition> straight = new ArrayList<>();
                straight.add(a);
                straight.add(b);
                segments.add(straight);
            }
        }

        generateRoutePresenter.prepareSuccessView(new GenerateRouteOutputData(segments));
    }
}
