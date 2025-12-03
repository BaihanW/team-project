package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkerTest {
    @Test
    void testMarker() {
        // 1. Create a location to put inside the marker
        Location loc = new Location("Test Point", 10.0, 20.0);

        // 2. Test Constructor
        Marker marker = new Marker(loc);

        // 3. Test Getters
        assertEquals(loc, marker.getLocation());
        assertEquals(10.0, marker.getLatitude());
        assertEquals(20.0, marker.getLongitude());
    }
}
