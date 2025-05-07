package utils;

import java.beans.PropertyChangeListener;

/**
 * Interface til objekter som skal have listeners
 */
public interface PropertyChangeSubject {
    /**
     * Tilføjer ny listener
     */
    void addListener(PropertyChangeListener listener);

    /**
     * Fjerner listener
     */
    void removeListener(PropertyChangeListener listener);
}
