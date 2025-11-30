package entity;

public class Marker {
    private final Location location;

    public Marker(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

    }
