package model;

import mediator.ServerRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Connection;
import java.util.ArrayList;

public class ChatModel implements Model, PropertyChangeListener {
    private Profiles profiles;
    private Messages messages;
    private Rooms rooms;
    private PropertyChangeSupport property;

    private ArrayList<ServerRequestHandler> requestHandlers;

    public ChatModel() {
        requestHandlers = new ArrayList<>();
        addHandler(UserFilesManager.getInstance());

        property = new PropertyChangeSupport(this);

        try (Connection connection = Database.getConnection()) {
            connection.prepareStatement("INSERT INTO profile (id, username) VALUES (0, 'System') ON CONFLICT DO NOTHING").executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException("grrr");
        }

        profiles = new ProfilesDBManager(this);
        messages = new MessagesDBManager(this);
        rooms = new RoomsDBManager(this);

        messages.addListener(this);
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public Messages getMessages() {
        return messages;
    }

    @Override
    public Rooms getRooms() {
        return rooms;
    }

    @Override
    public void passServerRequest(ServerRequest message) {
        for (ServerRequestHandler handler : requestHandlers) {
            handler.handleRequest(message);
        }
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

    @Override
    public void addHandler(ServerRequestHandler handler) {
        requestHandlers.add(handler);
    }

    @Override
    public void removeHandler(ServerRequestHandler handler) {
        requestHandlers.remove(handler);
    }
}
