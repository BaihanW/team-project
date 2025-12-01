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
            }
        }

        return new LoadedStops(names, positions);
    }

    public record LoadedStops(List<String> names, List<GeoPosition> positions) {
    }
}
