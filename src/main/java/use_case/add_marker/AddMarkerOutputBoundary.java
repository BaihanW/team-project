package use_case.add_marker;

public interface AddMarkerOutputBoundary {
    void prepareSuccessView(AddMarkerOutputData outputData);
    void prepareFailView(String errorMessage);
}
