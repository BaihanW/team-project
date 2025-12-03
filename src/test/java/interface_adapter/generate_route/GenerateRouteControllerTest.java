package interface_adapter.generate_route;

import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.generate_route.GenerateRouteInputBoundary;
import use_case.generate_route.GenerateRouteInputData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateRouteControllerTest {

    @Test
    void testGenerate() {
        // Arrange
        List<GeoPosition> stops = new ArrayList<>();
        stops.add(new GeoPosition(10.0, 20.0));
        stops.add(new GeoPosition(30.0, 40.0));
        String profile = "cycling";

        // Create a fake interactor to capture the input data
        final GenerateRouteInputData[] capturedData = new GenerateRouteInputData[1];
        GenerateRouteInputBoundary spyInteractor = new GenerateRouteInputBoundary() {
            @Override
            public void execute(GenerateRouteInputData inputData) {
                capturedData[0] = inputData;
            }
        };

        GenerateRouteController controller = new GenerateRouteController(spyInteractor);

        // Act
        controller.generate(profile, stops);

        // Assert
        GenerateRouteInputData result = capturedData[0];
        assertEquals(profile, result.getProfile());
        assertEquals(stops, result.getStops());
        assertEquals(2, result.getStops().size());
    }
}
