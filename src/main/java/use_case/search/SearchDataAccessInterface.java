package use_case.search;

import entity.Location;

import java.io.IOException;

/**
 * DAO interface for the Search Use Case.
 */
public interface SearchDataAccessInterface {

    /**
     * Checks if the given locationName exists.
     * @param locationName the locationName to look for
     * @return true if a user with the given locationName exists; false otherwise
     */
    boolean existsByName(String locationName) throws IOException, InterruptedException;

    /**
     * Saves the Location.
     * @param location the location to save
     */

    void save(Location location);

    /**
    void save(Location location);
     * Returns the user with the given username.
     * @param locationName the username to look up
     * @return the user with the given username
     */
    Location get(String locationName) throws IOException, InterruptedException;

    void setCurrentLocation(String locationName);

    String getCurrentLocationName();
}
