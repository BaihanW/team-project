package data_access;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import entity.Location;
import use_case.search.SearchDataAccessInterface;

/**
 * OSMDataAccessObject
 * A clean, testable Data Access Object for the Nominatim (OpenStreetMap) API.
 * <p>
 * Responsibilities:
 * - Perform HTTP requests to Nominatim
 * - Convert raw JSON into domain entities (Location)
 * - Handle API rate limiting and errors safely
 * <p>
 * This class contains NO business logic,
 * and conforms to the SearchDataAccessInterface.
 */
public class OSMDataAccessObject implements SearchDataAccessInterface {

    /** Recommended: only ONE client app-wide (keep-alive) */
    private final HttpClient client;

    /** Enforce Nominatim rate limit: max 1 request per second */
    private long lastRequestTimeMs = 0;

    public OSMDataAccessObject(HttpClient client) {
        this.client = client;
    }

    /** Enforces OpenStreetMap API rate limit. */
    private void applyRateLimit() {
        long now = System.currentTimeMillis();
        long diff = now - lastRequestTimeMs;

        if (diff < 1100) {
            try { Thread.sleep(1100 - diff); } catch (InterruptedException ignored) {}
        }
        lastRequestTimeMs = System.currentTimeMillis();
    }

    @Override
    public boolean existsByName(String locationName) throws IOException, InterruptedException {
        JSONArray array = fetchSearchResult(locationName, 1);
        return array.length() > 0;
    }

    @Override
    public Location get(String locationName) throws IOException, InterruptedException {
        JSONArray array = fetchSearchResult(locationName, 1);

        if (array.isEmpty()) {
            throw new IOException("No results found for: " + locationName);
        }

        JSONObject obj = array.getJSONObject(0);

        String name = obj.optString("display_name", locationName);
        double lat = obj.optDouble("lat", 0);
        double lon = obj.optDouble("lon", 0);

        return new Location(name, lat, lon);
    }

    /** Core helper: performs GET request and returns raw JSON search array. */
    @Override
    public List<Location> searchSuggestions(String query, int limit) throws IOException, InterruptedException {
        JSONArray array = fetchSearchResult(query, limit);

        List<Location> suggestions = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String name = obj.optString("display_name", query);
            double lat = obj.optDouble("lat", 0);
            double lon = obj.optDouble("lon", 0);
            suggestions.add(new Location(name, lat, lon));
        }

        return suggestions;
    }

    /** Core helper: performs GET request and returns raw JSON search array. */
    private JSONArray fetchSearchResult(String locationName, int limit)
            throws IOException, InterruptedException {

        applyRateLimit();

        String url = "https://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(locationName, StandardCharsets.UTF_8)
                + "&format=json&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(4))
                .header("User-Agent", "TripPlanner/1.0 (UofT CSC207)")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Nominatim API error: " + response.statusCode());
        }

        return new JSONArray(response.body());
    }
}