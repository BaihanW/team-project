package interface_adapter.displayRoute;

import use_case.displayRoute.DisplayRouteOutputBoundary;
import use_case.displayRoute.DisplayRouteOutputData;

public class DisplayRoutePresenter implements DisplayRouteOutputBoundary {

    private final DisplayRouteViewModel viewModel;

    public DisplayRoutePresenter(DisplayRouteViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(DisplayRouteOutputData outputData) {
        DisplayRouteState state = new DisplayRouteState();

        state.setLongitudes(outputData.getLongitudes());
        state.setLatitudes(outputData.getLatitudes());
        state.setMessage(outputData.getMessage());
        state.setSuccess(true);

        viewModel.setState(state);
    }

    @Override
    public void prepareFailureView(String message) {
        DisplayRouteState state = new DisplayRouteState();

        state.setSuccess(false);
        state.setMessage(message);
        state.setLatitudes(new double[0]);
        state.setLongitudes(new double[0]);

        viewModel.setState(state);

    }


}
