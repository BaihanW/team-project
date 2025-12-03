package use_case.add_marker;

import entity.Location;
import entity.Marker;
import use_case.search.SearchDataAccessInterface;

public class AddMarkerInteractor implements AddMarkerInputBoundary {
    private final AddMarkerDataAccessInterface markerDAO;
    private final SearchDataAccessInterface searchDAO;
    private final AddMarkerOutputBoundary presenter;

    public AddMarkerInteractor(AddMarkerDataAccessInterface markerDAO,
                               SearchDataAccessInterface searchDAO,
                               AddMarkerOutputBoundary presenter) {
        this.markerDAO = markerDAO;
        this.searchDAO = searchDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddMarkerInputData inputData) {
        double lat = inputData.getLatitude();
        double lon = inputData.getLongitude();

        String locationName;
        try {
            locationName = searchDAO.getNameFromCoordinates(lat, lon);
            if (locationName.length() > 30) {
                locationName = locationName.substring(0, 27) + "...";
            }
        } catch (Exception e) {
            locationName = String.format("%.5f, %.5f", lat, lon);
        }

        Location location = new Location(locationName, lat, lon);
        Marker marker = new Marker(location);

        markerDAO.save(marker);

        AddMarkerOutputData outputData = new AddMarkerOutputData(
                locationName, lat, lon
        );

        presenter.prepareSuccessView(outputData);
    }
}