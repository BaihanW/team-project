package interface_adapter.generate_route;

import interface_adapter.ViewModel;

public class GenerateRouteViewModel extends ViewModel<GenerateRouteState> {
    public GenerateRouteViewModel() {
        super("generate route");
        setState(new GenerateRouteState());
    }
}