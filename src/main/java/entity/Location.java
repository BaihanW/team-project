package entity;
/**
* Represents a geographical location with name, latitude, and longitude
* <p>
* this class is immutable
*/
public class Location {
    /* name of the location */
    private final String name;

    /* latitude in decimal */
    private final double latitude;

    /* longitude in decimal */
    private final double longitude;

    /**
     * Construct a new {@code Location} with the specified name, latitude, and longitude
     *
     * @param name
     * @param latitude
     * @param longitude
     */
    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the name of the location
     *
     * @return the name of the location
     */
    public String getName() { return name; }

    /**
     * Returns the latitude of the Location
     *
     * @return the latitude of the location
     */
    public double getLatitude() { return latitude; }

    /**
     * Returns the longitude of the Location
     *
     * @return the longitude of the location
     */
    public double getLongitude() { return longitude; }
}
