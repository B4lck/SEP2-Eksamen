package model;

public class ModelManager implements Model {

    private ProfileManager profileManager;
    private MessagesManager messagesManager;
    private RoomManager roomManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        messagesManager = new MessagesManager();
        roomManager = new RoomManager();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    @Override
    public RoomManager getRoomManager() {
        return roomManager;
    }

    @Override
    public UserFilesManager getUserFileManager() {
        return UserFilesManager.getInstance();
    }
}
