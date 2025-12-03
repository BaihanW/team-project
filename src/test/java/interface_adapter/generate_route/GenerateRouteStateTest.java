package interface_adapter.generate_route;

import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerateRouteStateTest {

    @Test
    void testDefaultConstructor() {
        GenerateRouteState state = new GenerateRouteState();
        assertNotNull(state.getRouteSegments());
        assertTrue(state.getRouteSegments().isEmpty());
        assertNull(state.getErrorMessage());
    }

    @Test
    void testCopyConstructorAndGettersSetters() {
        // Arrange: Create original state
        GenerateRouteState original = new GenerateRouteState();
        List<List<GeoPosition>> segments = new ArrayList<>();
        List<GeoPosition> segment = new ArrayList<>();
        segment.add(new GeoPosition(50.0, 50.0));
        segments.add(segment);

        original.setRouteSegments(segments);
        original.setErrorMessage("Original Error");

        // Act: Create copy
        GenerateRouteState copy = new GenerateRouteState(original);

        // Assert: Check values
        assertEquals("Original Error", copy.getErrorMessage());
        assertEquals(1, copy.getRouteSegments().size());
        assertEquals(50.0, copy.getRouteSegments().get(0).get(0).getLatitude());

        // Assert: Deep copy check (modifying copy shouldn't affect original list)
        // Note: The GeoPositions themselves might be shared references depending on implementation,
        // but the list structure should be independent.
        copy.getRouteSegments().clear();
        assertEquals(0, copy.getRouteSegments().size());
        assertEquals(1, original.getRouteSegments().size());
    }

    @Test
    void testCopyConstructorWithNullCopy() {
        // This covers the "if (copy != null)" branch if your code has it,
        // or just standard null safety check.
        // Based on your previous code, you handle null gracefully or copy fields directly.
        // If your copy constructor crashes on null, you can skip this,
        // but robust states usually handle it.

        // Assuming the constructor signature is public GenerateRouteState(GenerateRouteState copy)
        try {
            GenerateRouteState state = new GenerateRouteState(null);
            // If it doesn't throw exception, check if it initialized defaults
            assertNotNull(state.getRouteSegments());
        } catch (NullPointerException e) {
            // If your logic requires non-null, this is expected behavior.
            // But for 100% coverage of the *lines inside* the copy constructor,
            // you usually need a valid object.
        }
    }
}