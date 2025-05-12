package model;

public interface Model {
    ProfileManager getProfileManager();

    MessagesManager getMessagesManager();

    RoomManager getRoomManager();

    UserFilesManager getUserFileManager();
}
