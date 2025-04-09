package model;

import mediator.ServerMessage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ChatModel implements Model {
    private Profiles profiles;
    private PropertyChangeSupport property;

    public ChatModel() {
        property = new PropertyChangeSupport(this);
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public void passClientMessage(ServerMessage message) {
        profiles.handleMessage(message);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }
}
