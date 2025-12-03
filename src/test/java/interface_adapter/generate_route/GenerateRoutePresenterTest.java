package interface_adapter.generate_route;

import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.generate_route.GenerateRouteOutputData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerateRoutePresenterTest {

    @Test
    void testPrepareSuccessView() {
        // Arrange
        GenerateRouteViewModel viewModel = new GenerateRouteViewModel();
        GenerateRoutePresenter presenter = new GenerateRoutePresenter(viewModel);

        List<List<GeoPosition>> segments = new ArrayList<>();
        List<GeoPosition> segment1 = new ArrayList<>();
        segment1.add(new GeoPosition(1.0, 1.0));
        segments.add(segment1);

        GenerateRouteOutputData outputData = new GenerateRouteOutputData(segments);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        GenerateRouteState state = viewModel.getState();
        assertNotNull(state.getRouteSegments());
        assertEquals(1, state.getRouteSegments().size());
        assertNull(state.getErrorMessage());
    }

    @Test
    void testPrepareFailView() {
        // Arrange
        GenerateRouteViewModel viewModel = new GenerateRouteViewModel();
        GenerateRoutePresenter presenter = new GenerateRoutePresenter(viewModel);
        String errorMsg = "API Connection Failed";

        // Act
        presenter.prepareFailView(errorMsg);

        // Assert
        GenerateRouteState state = viewModel.getState();
        assertEquals(errorMsg, state.getErrorMessage());
    }
}