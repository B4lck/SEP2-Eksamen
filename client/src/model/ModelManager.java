package model;

import mediator.ChatClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

public class ModelManager implements Model, PropertyChangeListener {
    private PropertyChangeSupport property;
    private ChatClient client;

    private ProfileManager profileManager;

    public ModelManager(ChatClient client) {
        this.property = new PropertyChangeSupport(this);
        this.client = client;
        profileManager = new ProfileManager(client);
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
    public ProfileManager getProfiles() {
        return profileManager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
