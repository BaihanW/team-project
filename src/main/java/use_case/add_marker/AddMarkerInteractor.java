package use_case.add_marker;


import entity.Location;
import entity.Marker;


import java.util.List;


public class AddMarkerInteractor implements AddMarkerInputBoundary {


    private final AddMarkerDataAccessInterface markerDataAccess;
    private final AddMarkerOutputBoundary addMarkerPresenter;


    public AddMarkerInteractor(AddMarkerDataAccessInterface markerDataAccess,
                               AddMarkerOutputBoundary addMarkerPresenter) {
        this.markerDataAccess = markerDataAccess;
        this.addMarkerPresenter = addMarkerPresenter;
    }


    @Override
    public void execute(AddMarkerInputData inputData) {
        // 1) 입력값을 엔티티로 변환
        //    Location 생성 방식은 너희 Location 생성자에 맞게 조정해.
        //    여기서는 이름 없이 좌표만 쓰는 예시로 "" 사용.
        Location location = new Location(
                "",                         // 또는 inputData에서 이름 받아도 됨
                inputData.getLatitude(),
                inputData.getLongitude()
        );


        Marker marker = new Marker(location);


        try {
            // 2)
            if (markerDataAccess.exists(location)) {
                addMarkerPresenter.prepareFailView("Marker already exists at this location.");
                return;
            }


            // 3)
            markerDataAccess.save(marker);


            // 4)
            List<Marker> allMarkers = markerDataAccess.allMarkers();


            // 5)
            AddMarkerOutputData outputData = new AddMarkerOutputData(
                    location.getLatitude(),
                    location.getLongitude()

            );


            addMarkerPresenter.prepareSuccessView(outputData);


        } catch (Exception e) {
            addMarkerPresenter.prepareFailView(
                    "Unexpected error while adding marker: " + e.getMessage()
            );
        }
    }
}
