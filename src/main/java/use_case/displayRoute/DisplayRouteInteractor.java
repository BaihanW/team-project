package use_case.displayRoute;

import interface_adapter.displayRoute.DisplayRoutePresenter;
import use_case.displayRoute.DisplayRouteOutputBoundary;

import java.io.IOException;

public class DisplayRouteInteractor implements DisplayRouteInputBoundary {
    private final DisplayRouteDataAccessInterface displayRouteDataAccess;
    private final DisplayRouteOutputBoundary displayRouteOutputBoundary;

    /**
     * constructs the display route interactor
     *
     * @param displayRouteDataAccess     the data access object used to complete route
     * @param displayRouteOutputBoundary the presenter used to format output for the view
     */

    public DisplayRouteInteractor(DisplayRouteDataAccessInterface displayRouteDataAccess,
                                  DisplayRouteOutputBoundary displayRouteOutputBoundary) {
        this.displayRouteDataAccess = displayRouteDataAccess;
        this.displayRouteOutputBoundary = displayRouteOutputBoundary;
    }

    @Override
    public void execute(DisplayRouteInputData inputData) {
        double startLong = inputData.getStartLongitude();
        double startLat = inputData.getStartLatitude();
        double endLong = inputData.getEndLongitude();
        double endLat = inputData.getEndLatitude();

        try {
            RouteResponseModel routeResponse =
                    displayRouteDataAccess.getRoute(startLat, startLong, endLat, endLong);

            DisplayRouteOutputData outputData = new DisplayRouteOutputData(routeResponse.getLongitudes(),
                    routeResponse.getLatitudes(),
                    routeResponse.getDistanceInMeters(),
                    routeResponse.getDurationInSeconds(),
                    true,
                    "route loaded successfully");

            displayRouteOutputBoundary.prepareSuccessView(outputData);

        } catch (IOException e) {
            displayRouteOutputBoundary.prepareFailureView(
                    "Network error while calculating route: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            displayRouteOutputBoundary.prepareFailureView(
                    "Request interrupted while calculating route."
            );
        } catch (Exception e) {
            displayRouteOutputBoundary.prepareFailureView(
                    "Unexpected error while calculating route: " + e.getMessage()
            );
        }
    }
}
