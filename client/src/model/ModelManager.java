package model;

public class ModelManager implements Model {

    private ProfileManager profileManager;
    private ChatManager chatManager;
    private ChatRoomManager chatRoomManager;

    public ModelManager() {
        profileManager = new ProfileManager();
        chatManager = new ChatManager();
        chatRoomManager = new ChatRoomManager();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public ChatRoomManager getChatRoomManager() {
        return chatRoomManager;
    }
}
