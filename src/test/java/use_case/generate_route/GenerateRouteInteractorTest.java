package use_case.generate_route;

import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class GenerateRouteInteractorTest {

    // 1. Test failure when fewer than 2 stops are provided
    @Test
    void failureInsufficientStopsTest() {
        List<GeoPosition> stops = new ArrayList<>();
        stops.add(new GeoPosition(43.6532, -79.3832));

        GenerateRouteInputData inputData = new GenerateRouteInputData("walking", stops);

        GenerateRouteOutputBoundary failurePresenter = new GenerateRouteOutputBoundary() {
            @Override
            public void prepareSuccessView(GenerateRouteOutputData outputData) {
                fail("Should not succeed with fewer than 2 stops.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("Add at least two stops to compute a full route.", error);
            }
        };

        GenerateRouteRoutingDataAccessInterface dummyDAO = (s, e, p) -> null;
        GenerateRouteInteractor interactor = new GenerateRouteInteractor(dummyDAO, failurePresenter);
        interactor.execute(inputData);
    }

    // 2. Test success when DAO returns a valid route
    @Test
    void successRouteFoundTest() {
        GeoPosition start = new GeoPosition(43.65, -79.38);
        GeoPosition end = new GeoPosition(43.66, -79.39);
        List<GeoPosition> stops = Arrays.asList(start, end);
        List<GeoPosition> expectedRoute = Arrays.asList(start, new GeoPosition(43.655, -79.385), end);

        GenerateRouteInputData inputData = new GenerateRouteInputData("driving", stops);

        GenerateRouteRoutingDataAccessInterface successDAO = (s, e, p) -> expectedRoute;

        GenerateRouteOutputBoundary successPresenter = new GenerateRouteOutputBoundary() {
            @Override
            public void prepareSuccessView(GenerateRouteOutputData outputData) {
                assertEquals(1, outputData.getSegments().size());
                assertEquals(expectedRoute, outputData.getSegments().get(0));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Should not fail when route is found.");
            }
        };

        GenerateRouteInteractor interactor = new GenerateRouteInteractor(successDAO, successPresenter);
        interactor.execute(inputData);
    }

    // 3. Test fallback when DAO returns empty list (covers !segment.isEmpty() branch)
    @Test
    void successFallbackOnEmptyRouteTest() {
        GeoPosition start = new GeoPosition(10.0, 10.0);
        GeoPosition end = new GeoPosition(20.0, 20.0);
        List<GeoPosition> stops = Arrays.asList(start, end);

        GenerateRouteInputData inputData = new GenerateRouteInputData("walking", stops);

        // Return empty list
        GenerateRouteRoutingDataAccessInterface emptyDAO = (s, e, p) -> new ArrayList<>();

        GenerateRouteOutputBoundary successPresenter = new GenerateRouteOutputBoundary() {
            @Override
            public void prepareSuccessView(GenerateRouteOutputData outputData) {
                List<List<GeoPosition>> segments = outputData.getSegments();
                assertEquals(1, segments.size());
                assertEquals(2, segments.get(0).size()); // Straight line has 2 points
            }

            @Override
            public void prepareFailView(String error) {
                fail("Should fallback to straight line.");
            }
        };

        GenerateRouteInteractor interactor = new GenerateRouteInteractor(emptyDAO, successPresenter);
        interactor.execute(inputData);
    }

    // 4. [New] Test fallback when DAO returns NULL (covers segment != null branch)
    @Test
    void successFallbackOnNullRouteTest() {
        GeoPosition start = new GeoPosition(10.0, 10.0);
        GeoPosition end = new GeoPosition(20.0, 20.0);
        List<GeoPosition> stops = Arrays.asList(start, end);

        GenerateRouteInputData inputData = new GenerateRouteInputData("walking", stops);

        // Return NULL explicitly
        GenerateRouteRoutingDataAccessInterface nullDAO = (s, e, p) -> null;

        GenerateRouteOutputBoundary successPresenter = new GenerateRouteOutputBoundary() {
            @Override
            public void prepareSuccessView(GenerateRouteOutputData outputData) {
                List<List<GeoPosition>> segments = outputData.getSegments();
                assertEquals(1, segments.size());
                // Verify straight line fallback
                assertEquals(start, segments.get(0).get(0));
                assertEquals(end, segments.get(0).get(1));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Should fallback to straight line on null.");
            }
        };

        GenerateRouteInteractor interactor = new GenerateRouteInteractor(nullDAO, successPresenter);
        interactor.execute(inputData);
    }

    // 5. Test fallback on Exception
    @Test
    void successFallbackOnExceptionTest() {
        GeoPosition start = new GeoPosition(50.0, 50.0);
        GeoPosition end = new GeoPosition(51.0, 51.0);
        List<GeoPosition> stops = Arrays.asList(start, end);

        GenerateRouteInputData inputData = new GenerateRouteInputData("bicycling", stops);

        GenerateRouteRoutingDataAccessInterface exceptionDAO = (s, e, p) -> {
            throw new RuntimeException("API error");
        };

        GenerateRouteOutputBoundary successPresenter = new GenerateRouteOutputBoundary() {
            @Override
            public void prepareSuccessView(GenerateRouteOutputData outputData) {
                assertEquals(1, outputData.getSegments().size());
                assertEquals(start, outputData.getSegments().get(0).get(0));
            }

            @Override
            public void prepareFailView(String error) {
                fail("Should fallback to straight line on exception.");
            }
        };

        GenerateRouteInteractor interactor = new GenerateRouteInteractor(exceptionDAO, successPresenter);
        interactor.execute(inputData);
    }
}