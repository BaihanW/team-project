package interface_adapter.displayRoute;

/**
 * the state for display route view
 */

public class DisplayRouteState {
    private double[] latitudes = new double[0];
    private double[] longitudes = new double[0];

    private String message = "";
    private boolean success = true;

    public double[] getLatitudes() {
        return latitudes;
    }

    public void setLatitudes(double[] latitudes) {
        this.latitudes = latitudes;
    }

    public double[] getLongitudes() {
        return longitudes;
    }

    public void setLongitudes(double[] longitudes) {
        this.longitudes = longitudes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
