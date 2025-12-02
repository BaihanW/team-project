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

    public void save(Location location) {
    }

    public void setCurrentLocation(String locationName) {
    }

    public String getCurrentLocationName() {
        return "";
    }

    public Location reverse(double latitude, double longitude) throws IOException, InterruptedException {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                + latitude + "&lon=" + longitude + "&zoom=18&addressdetails=0";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(4))
                .header("User-Agent", "TripPlanner/1.0 (UofT CSC207)")
                .GET()
                .build();

        HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Reverse geocode failed: " + response.statusCode());
        }

        JSONObject obj = new JSONObject(response.body());
        String name = obj.optString("display_name", String.format("%.5f, %.5f", latitude, longitude));
        double lat = obj.optDouble("lat", latitude);
        double lon = obj.optDouble("lon", longitude);
        return new Location(name, lat, lon);
    }

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

    public List<Location> searchSuggestions(String query, Double minLon, Double minLat, Double maxLon, Double maxLat, int limit) throws IOException, InterruptedException {
        if (query == null || query.isBlank()) return new ArrayList<>();
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?q=" + encoded + "&format=json&limit=" + limit + "&addressdetails=0";
        if (minLon != null && minLat != null && maxLon != null && maxLat != null) {
            url += "&viewbox=" + minLon + "," + minLat + "," + maxLon + "," + maxLat + "&bounded=0";
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(4))
                .header("User-Agent", "TripPlanner/1.0 (UofT CSC207)")
                .GET()
                .build();

        HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Search request failed: " + response.statusCode());
        }

        JSONArray arr = new JSONArray(response.body());
        List<Location> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String name = o.optString("display_name", null);
            double lat = o.optDouble("lat", Double.NaN);
            double lon = o.optDouble("lon", Double.NaN);
            if (name != null && !Double.isNaN(lat) && !Double.isNaN(lon)) {
                out.add(new Location(name, lat, lon));
            }
        }
        return out;
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