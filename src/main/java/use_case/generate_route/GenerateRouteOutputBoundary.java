package use_case.generate_route;

public interface GenerateRouteOutputBoundary {
    void prepareSuccessView(GenerateRouteOutputData outputData);

    void prepareFailView(String error);
}
