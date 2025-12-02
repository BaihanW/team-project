package interface_adapter.generate_route;

import use_case.generate_route.GenerateRouteOutputBoundary;
import use_case.generate_route.GenerateRouteOutputData;

public class GenerateRoutePresenter implements GenerateRouteOutputBoundary {

    private final GenerateRouteViewModel routeViewModel;

    public GenerateRoutePresenter(GenerateRouteViewModel routeViewModel) {
        this.routeViewModel = routeViewModel;
    }

    @Override
    public void prepareSuccessView(GenerateRouteOutputData outputData) {
        GenerateRouteState state = new GenerateRouteState(routeViewModel.getState());
        state.setRouteSegments(outputData.getSegments());
        state.setErrorMessage(null);
        routeViewModel.setState(state);
        routeViewModel.firePropertyChange("route", state);
    }

    @Override
    public void prepareFailView(String error) {
        GenerateRouteState state = new GenerateRouteState(routeViewModel.getState());
        state.setErrorMessage(error);
        routeViewModel.setState(state);
        routeViewModel.firePropertyChange("error", state);
    }
}
