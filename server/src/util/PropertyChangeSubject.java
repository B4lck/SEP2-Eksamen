package util;

import java.beans.PropertyChangeListener;

/**
 * Interface til objekter som skal have listeners
 */
public interface PropertyChangeSubject {
    /**
     * Tilføjer ny listener
     * @param listener
     */
    void addListener(PropertyChangeListener listener);

    /**
     * Fjerner listener
     * @param listener
     */
    void removeListener(PropertyChangeListener listener);
}
