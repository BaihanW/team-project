package interface_adapter.addMarker;


import use_case.add_marker.AddMarkerOutputBoundary;
import use_case.add_marker.AddMarkerOutputData;


/**
 * Presenter for the Add Marker Use Case.
 */
public class AddMarkerPresenter implements AddMarkerOutputBoundary {


    private final AddMarkerViewModel addMarkerViewModel;


    public AddMarkerPresenter(AddMarkerViewModel addMarkerViewModel) {
        this.addMarkerViewModel = addMarkerViewModel;
    }


    @Override
    public void prepareSuccessView(AddMarkerOutputData outputData) {
        AddMarkerState newState = new AddMarkerState();
        newState.setLastMarkerLatitude(outputData.getLatitude());
        newState.setLastMarkerLongitude(outputData.getLongitude());
        newState.setErrorMessage(null);


        addMarkerViewModel.setState(newState);
        addMarkerViewModel.firePropertyChange();
    }


    @Override
    public void prepareFailView(String errorMessage) {
        AddMarkerState newState = new AddMarkerState();
        newState.setErrorMessage(errorMessage);


        addMarkerViewModel.setState(newState);
        addMarkerViewModel.firePropertyChange();
    }
}
