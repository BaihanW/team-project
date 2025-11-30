package use_case.remove_marker;

public interface RemoveMarkerOutputBoundary {
    void prepareSuccessView(RemoveMarkerOutputData outputData);

    void prepareFailView(String error);
}
