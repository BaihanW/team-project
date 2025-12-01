package use_case.search;

import entity.Location;

import java.io.IOException;
import java.util.List;

public interface SearchDataAccessInterface {

    boolean existsByName(String locationName) throws IOException, InterruptedException;

    Location get(String locationName) throws IOException, InterruptedException;

    List<Location> searchSuggestions(String query, int limit) throws IOException, InterruptedException;

}
