package model;

import mediator.ChatClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

public class ModelManager implements Model, PropertyChangeListener {
    private PropertyChangeSupport property;
    private ChatClient client;

    public ModelManager(ChatClient client) {
        this.property = new PropertyChangeSupport(this);
        this.client = client;
        client.addListener(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    @Override
    public ArrayList<Profile> getProfiles() {
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
