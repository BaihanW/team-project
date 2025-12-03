package use_case.add_marker;

import entity.Location;
import entity.Marker;

public class AddMarkerInteractor implements AddMarkerInputBoundary {
    private final AddMarkerDataAccessInterface addMarkerAccessObj;
    private final AddMarkerOutputBoundary addMarkerPresenter;

    public AddMarkerInteractor(AddMarkerDataAccessInterface addMarkerAccess,
                               AddMarkerOutputBoundary addMarkerPresenter) {
        this.addMarkerAccessObj = addMarkerAccess;
        this.addMarkerPresenter = addMarkerPresenter;
    }

    @Override
    public void execute(AddMarkerInputData inputData) {
        double lat = inputData.getLatitude();
        double lon = inputData.getLongitude();

        Location location = new Location("", lat, lon);

        if (addMarkerAccessObj.exists(location)) {
            addMarkerPresenter.prepareFailView("A marker already exists at this location");
            return;
        }

        Marker marker = new Marker(location);
        addMarkerAccessObj.save(marker);

        AddMarkerOutputData outputData=
                new  AddMarkerOutputData(lat, lon);
        addMarkerPresenter.prepareSuccessView(outputData);
    }



}
