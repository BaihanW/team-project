package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void testConstructorAndGetters() {
        Location loc = new Location("Toronto", 43.65, -79.38);

        assertEquals("Toronto", loc.getName());
        assertEquals(43.65, loc.getLatitude());
        assertEquals(-79.38, loc.getLongitude());
    }
}
