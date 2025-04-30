package model;

public class ModelManager implements Model {

    private ProfileManager profileManager;
    private ChatManager chatManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        chatManager = new ChatManager();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ChatManager getChatRoomManager() {
        return chatManager;
    }
}
