package interface_adapter.displayRoute;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DisplayRouteViewModel {

    public static final String routeStateProperty = "routeState";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private DisplayRouteState state = new DisplayRouteState();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void setState(DisplayRouteState state) {
        this.state = state;
        support.firePropertyChange(routeStateProperty, null, this.state);
    }

    public DisplayRouteState getState() {
        return state;
    }
}
