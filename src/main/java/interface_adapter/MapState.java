package interface_adapter;

public class MapState {
    private double centerLatitude;
    private double centerLongitude;
    private int zoomLevel;

    public MapState(double centerLatitude, double centerLongitude, int zoomLevel) {
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.zoomLevel = zoomLevel;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public void setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}
