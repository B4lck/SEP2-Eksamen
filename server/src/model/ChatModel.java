package model;

import mediator.ServerRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ChatModel implements Model, PropertyChangeListener {
    private Profiles profiles;
    private ChatRooms chatRooms;
    private PropertyChangeSupport property;

    public ChatModel() {
        property = new PropertyChangeSupport(this);
        profiles = new ProfilesArrayListManager();
        chatRooms = new ChatRoomsArrayListManager();

        chatRooms.addListener(this);
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public void passClientMessage(ServerRequest message) {
        profiles.handleMessage(message);
        chatRooms.handleMessage(message);
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
    public void propertyChange(PropertyChangeEvent evt) {
        property.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}
