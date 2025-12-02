package use_case.save_stops;

public interface SaveStopsOutputBoundary {
    void presentSuccess();
    void presentFailure(String error);
}

