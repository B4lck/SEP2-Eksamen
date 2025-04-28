package model;

import mediator.ChatClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ModelManager implements Model, PropertyChangeListener {

    private ProfileManager profileManager;
    private ChatRoomManager chatRoomManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        chatRoomManager = new ChatRoomManager();
        ChatClient.getInstance().addListener(this);
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ChatRoomManager getChatRoomManager() {
        return chatRoomManager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
