package use_case.displayRoute;

/**
 * the output boundary for display route use case.
 */

public interface DisplayRouteOutputBoundary {

    /**
     * prepares the success view for display route use case.
     * @param displayRouteOutputData the output data containing route information
     */
    void prepareSuccessView(DisplayRouteOutputData displayRouteOutputData);

    /**
     * prepares the fail view for display route use case.
     * @param message the explanation of the failure
     */
    void prepareFailureView(String message);
}
