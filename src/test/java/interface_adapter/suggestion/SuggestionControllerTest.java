package interface_adapter.suggestion;

import org.junit.jupiter.api.Test;
import use_case.suggestion.SuggestionInputBoundary;
import use_case.suggestion.SuggestionInputData;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SuggestionControllerTest {

    @Test
    void controllerDelegatesToInteractor() {
        AtomicReference<SuggestionInputData> captured = new AtomicReference<>();
        SuggestionInputBoundary boundary = captured::set;
        SuggestionController controller = new SuggestionController(boundary);

        controller.execute("query text");

        assertNotNull(captured.get());
        assertEquals("query text", captured.get().getQuery());
    }
}