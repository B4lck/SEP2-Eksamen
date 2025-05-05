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
        profiles = new ProfilesArrayListManager(this);
        chats = new ChatArrayListManager(this);
        chatRooms = new ChatRoomsArrayListManager(this);

        chats.addListener(this);

        // Dummy data

        var user1 = profiles.createProfile("1", "1");
        var user2 = profiles.createProfile("Mazen Laursen", "1234");
        profiles.createProfile("Malthe Balck", "1234");
        profiles.createProfile("Nikolai Sharaf", "1234");
        profiles.createProfile("Bruger5", "1234");
        profiles.createProfile("Bruger6", "1234");
        profiles.createProfile("Bruger7", "1234");
        profiles.createProfile("Bruger8", "1234");

        var rum = chatRooms.createRoom("Rum nr 1", user2.getUUID());
        chatRooms.addUser(rum.getRoomId(), user1.getUUID(), user2.getUUID());
        chatRooms.createRoom("Rum nr 2", user1.getUUID());
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public Chat getChat() {
        return chats;
    }

    @Override
    public ChatRooms getChatRooms() {
        return chatRooms;
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
