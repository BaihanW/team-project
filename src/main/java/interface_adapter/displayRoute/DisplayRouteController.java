package interface_adapter.displayRoute;

import use_case.displayRoute.DisplayRouteInputBoundary;
import use_case.displayRoute.DisplayRouteInputData;

/**
 * controller for display route use case
 * called by UI
 */

public class DisplayRouteController {

    private final DisplayRouteInputBoundary inputBoundary;

    public DisplayRouteController(DisplayRouteInputBoundary displayRouteInputBoundary) {
        this.inputBoundary = displayRouteInputBoundary;
    }

    /**
     * Called by the UI when the user wants to display a route between two coordinates.
     *
     * @param startLat  latitude of the starting point
     * @param startLon longitude of the starting point
     * @param endLat    latitude of the destination
     * @param endLon   longitude of the destination
     */

    public void displayRoute(double startLat, double startLon, double endLat, double endLon) {

        DisplayRouteInputData inputData = new DisplayRouteInputData(startLat, startLon, endLat, endLon);
    }


}
