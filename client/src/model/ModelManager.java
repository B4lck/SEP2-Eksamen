package model;

public class ModelManager implements Model {

    private ProfileManager profileManager;
    private ChatRoomManager chatRoomManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        chatRoomManager = new ChatRoomManager();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ChatRoomManager getChatRoomManager() {
        return chatRoomManager;
    }
}
