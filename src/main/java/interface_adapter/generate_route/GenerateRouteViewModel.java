package interface_adapter.generate_route;

import interface_adapter.ViewModel;

public class GenerateRouteViewModel extends ViewModel<GenerateRouteState> {

    private GenerateRouteState state = new GenerateRouteState();

    public GenerateRouteViewModel() {
        super("generate route");
    }

    public GenerateRouteState getState() {
        return state;
    }

    public void setState(GenerateRouteState state) {
        this.state = state;
    }

    @Override
    public void firePropertyChange() {
        super.firePropertyChange("state", state);
    }
}
