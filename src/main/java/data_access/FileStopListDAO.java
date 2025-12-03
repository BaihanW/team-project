package data_access;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.save_stops.SaveStopsDataAccessInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * A file-based implementation for persisting saved stops.
 */
public class FileStopListDAO implements SaveStopsDataAccessInterface {

    private final Path filePath;

    public FileStopListDAO(String directory) {
        Path dirPath = Paths.get(directory);
        this.filePath = dirPath.resolve("stops.txt");
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create stop list directory", e);
        }
    }

    @Override
    public void save(List<String> names, List<GeoPosition> positions) throws IOException {
        if (names.size() != positions.size()) {
            throw new IllegalArgumentException("Names and positions must be the same length");
        }

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            GeoPosition position = positions.get(i);
            lines.add(String.format("%s\t%f\t%f", names.get(i), position.getLatitude(), position.getLongitude()));
        }

        Files.write(filePath, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStopListDAO implements SaveStopsDataAccessInterface {

    private final File file;

    public FileStopListDAO(String directory) {
        this.file = new File(directory, "stoplist.txt");
    }

    /**
     * File format:
     *   name,lat,lon
     *   name,lat,lon
     * returns:
     *   List<String> stopNames
     *   List<GeoPosition> stopPositions
     */
    public LoadedStops load() throws IOException {
        List<String> names = new ArrayList<>();
        List<GeoPosition> positions = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return new LoadedStops(names, positions);
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t");
            if (parts.length < 3) {
                continue;
            }

            try {
                double latitude = Double.parseDouble(parts[1]);
                double longitude = Double.parseDouble(parts[2]);
                names.add(parts[0]);
                positions.add(new GeoPosition(latitude, longitude));
            } catch (NumberFormatException ignored) {
                // Skip malformed lines
        if (!file.exists()) {
            return new LoadedStops(names, positions);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length != 3) continue;

                String name = p[0];
                double lat = Double.parseDouble(p[1]);
                double lon = Double.parseDouble(p[2]);

                names.add(name);
                positions.add(new GeoPosition(lat, lon));
            }
        }

        return new LoadedStops(names, positions);
    }

    public record LoadedStops(List<String> names, List<GeoPosition> positions) {
    /**
     * Save names + positions (GeoPosition)
     */
    public void save(List<String> names, List<GeoPosition> positions) throws IOException {
        System.out.println("Interactor received stops: ");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (int i = 0; i < names.size(); i++) {
                GeoPosition p = positions.get(i);
                out.println(names.get(i) + ";" + p.getLatitude() + ";" + p.getLongitude());
            }
        }
    }

    /** DTO class storing two lists */
    public static class LoadedStops {
        public final List<String> names;
        public final List<GeoPosition> positions;

        public LoadedStops(List<String> names, List<GeoPosition> positions) {
            this.names = names;
            this.positions = positions;
        }
    }
}
