package data_access;

import org.jxmapviewer.viewer.GeoPosition;
import use_case.save_stops.SaveStopsDataAccessInterface;
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