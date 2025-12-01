package use_case.add_marker;

/**
 * Output boundary for addMarker use case
 */
public interface AddMarkerOutputBoundary {
    /**
     * @param  outputData the output data
     */
    void prepareSuccessView(AddMarkerOutputData outputData);

    /**
     * @param errorMessage the explanation for the failure
     */
    void prepareFailView(String errorMessage);
}
