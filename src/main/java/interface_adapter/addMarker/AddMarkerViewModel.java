package interface_adapter.addMarker;

import interface_adapter.ViewModel;

public class AddMarkerViewModel extends ViewModel<AddMarkerState> {

    public AddMarkerViewModel() {
        super("add marker");
        this.setState(new AddMarkerState());
    }
}
