package interface_adapter.generate_route;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenerateRouteViewModelTest {

    @Test
    void testInitialization() {
        GenerateRouteViewModel viewModel = new GenerateRouteViewModel();

        assertEquals("generate route", viewModel.getViewName());
        assertNotNull(viewModel.getState());
        assertTrue(viewModel.getState().getRouteSegments().isEmpty());
    }
}