package model;

import mediator.ServerRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ChatModel implements Model, PropertyChangeListener {
    private Profiles profiles;
    private Chat chats;
    private ChatRooms chatRooms;
    private PropertyChangeSupport property;

    public ChatModel() {
        property = new PropertyChangeSupport(this);
        profiles = new ProfilesArrayListManager();
        chats = new ChatArrayListManager();
        chatRooms = new ChatRoomsArrayListManager();

        chats.addListener(this);

        // Dummy data

        profiles.addProfile(new ArrayListProfile("Nikolai Sharaf", "1234"));
        profiles.addProfile(new ArrayListProfile("Mazen Laursen", "1234"));
        profiles.addProfile(new ArrayListProfile("Malthe Balck", "1234"));
        profiles.addProfile(new ArrayListProfile("Bruger4", "1234"));
        profiles.addProfile(new ArrayListProfile("Bruger5", "1234"));
        profiles.addProfile(new ArrayListProfile("Bruger6", "1234"));
        profiles.addProfile(new ArrayListProfile("Bruger7", "1234"));
        profiles.addProfile(new ArrayListProfile("Bruger8", "1234"));
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public void passClientMessage(ServerRequest message) {
        profiles.handleMessage(message);
        chats.handleMessage(message);
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
