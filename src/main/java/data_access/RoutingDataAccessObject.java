package data_access;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;
import use_case.generate_route.GenerateRouteRoutingDataAccessInterface;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RoutingDataAccessObject implements GenerateRouteRoutingDataAccessInterface {
    private final HttpClient client;

    public RoutingDataAccessObject(HttpClient client) {
        this.client = client;
    }

    public List<GeoPosition> getRoute(GeoPosition src, GeoPosition dst, String profile) throws IOException, InterruptedException {
        if (src == null || dst == null) return new ArrayList<>();

        String coords = String.format("%f,%f;%f,%f", src.getLongitude(), src.getLatitude(), dst.getLongitude(), dst.getLatitude());
        String url = "https://router.project-osrm.org/route/v1/" + profile + "/" + coords + "?overview=full&geometries=geojson";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "TripPlanner/1.0 (207 5-6)")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Routing request failed with code: " + resp.statusCode());
        }

        JSONObject root = new JSONObject(resp.body());
        JSONArray routes = root.optJSONArray("routes");
        if (routes == null || routes.isEmpty()) return new ArrayList<>();

        JSONObject first = routes.getJSONObject(0);
        JSONObject geometry = first.getJSONObject("geometry");
        JSONArray coordsArray = geometry.getJSONArray("coordinates");

        List<GeoPosition> result = new ArrayList<>(coordsArray.length());
        for (int i = 0; i < coordsArray.length(); i++) {
            JSONArray p = coordsArray.getJSONArray(i);
            double lon = p.getDouble(0);
            double lat = p.getDouble(1);
            result.add(new GeoPosition(lat, lon));
        }

        return result;
    }
}

