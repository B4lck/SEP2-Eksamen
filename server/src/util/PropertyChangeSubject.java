package util;

import java.beans.PropertyChangeListener;

public interface PropertyChangeSubject {
    void addListener(PropertyChangeListener listener);
    void removeListener(PropertyChangeListener listener);
}
