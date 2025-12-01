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
            // 2) 도메인 규칙: 같은 위치에 두 번 찍지 않기
            if (markerDataAccess.exists(location)) {
                addMarkerPresenter.prepareFailView("Marker already exists at this location.");
                return;
            }

            // 3) 저장
            markerDataAccess.save(marker);

            // 4) 필요하면 전체 마커 목록 가져오기
            List<Marker> allMarkers = markerDataAccess.allMarkers();

            // 5) OutputData 만들어서 Presenter에 전달
            AddMarkerOutputData outputData = new AddMarkerOutputData(
                    location.getLatitude(),
                    location.getLongitude()
                    // 필요하면 allMarkers 도 필드로 넣을 수 있음
            );

            addMarkerPresenter.prepareSuccessView(outputData);

        } catch (Exception e) {
            addMarkerPresenter.prepareFailView(
                    "Unexpected error while adding marker: " + e.getMessage()
            );
        }
    }
}

