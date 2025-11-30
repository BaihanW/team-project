package data_access;

import entity.Location;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OSMDataAccessObject.
 *
 * These tests:
 *  - Access the real OpenStreetMap Nominatim API
 *  - Are slow (DAO has built-in 1-second rate limit)
 *  - May fail if internet or API is down
 *
 * YOU requested DAO to remain unchanged, so these tests avoid mocking
 * and hit the real server.
 */
class OSMDataAccessObjectTest {

    private final OSMDataAccessObject dao =
            new OSMDataAccessObject(HttpClient.newHttpClient());

    @Test
    void testExistsByName_knownCity() throws Exception {
        boolean result = dao.existsByName("Toronto");
        assertTrue(result, "Toronto should exist in Nominatim API");
    }

    @Test
    void testExistsByName_nonexistentPlace() throws Exception {
        boolean result = dao.existsByName("asdasdasdasdasdasd_nonexistent_place");
        assertFalse(result, "Random string should not match any location");
    }

    @Test
    void testGet_knownCity() throws Exception {
        Location toronto = dao.get("Toronto");

        assertNotNull(toronto);
        assertEquals("Toronto", toronto.getName().substring(0, 7),
                "Returned name should include 'Toronto'");

        // Toronto approx lat/lon
        assertTrue(toronto.getLatitude() > 40 && toronto.getLatitude() < 50);
        assertTrue(toronto.getLongitude() < -70 && toronto.getLongitude() > -90);
    }

    @Test
    void testGet_unknownLocation_throws() {
        assertThrows(IOException.class, () -> {
            dao.get("wqdqwdqwdqwdqwdqwd_nonexistent_place");
        });
    }

    @Test
    void testRateLimit_delay() throws Exception {
        long start = System.currentTimeMillis();

        dao.existsByName("Toronto");   // first call
        dao.existsByName("Paris");     // second call → must wait ≥1 second

        long elapsed = System.currentTimeMillis() - start;

        // The DAO enforces 1100ms minimum gap
        assertTrue(elapsed >= 1000,
                "Two consecutive calls must respect built-in rate limit");
    }
}
