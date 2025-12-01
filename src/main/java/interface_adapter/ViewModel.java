package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ViewModel<T> {

    private final String viewName;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private T state;

    public ViewModel(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return this.viewName;
    }

    public T getState() {
        return this.state;
    }

    public void setState(T state) {
        this.state = state;
    }

    public void firePropertyChange() {
        this.support.firePropertyChange("state", null, this.state);
    }

    protected void firePropertyChange(String propertyName, Object value) {
        support.firePropertyChange(propertyName, null, value);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void showSaveSuccessMessage(String s) {

    }

    public void showSaveErrorMessage(String error) {

    }
}

