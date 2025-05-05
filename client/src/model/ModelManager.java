package model;

public class ModelManager implements Model {

    private ProfileManager profileManager;
    private MessagesManager chatManager;
    private RoomManager chatRoomManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        chatManager = new MessagesManager();
        chatRoomManager = new RoomManager();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public MessagesManager getMessagesManager() {
        return chatManager;
    }

    @Override
    public RoomManager getRoomManager() {
        return chatRoomManager;
    }
}
