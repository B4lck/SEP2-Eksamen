package model;

import mediator.ChatClient;
import util.ServerError;

import java.io.File;

public class UserFileManager {

    static private UserFileManager instance;

    private ChatClient chatClient = ChatClient.getInstance();

    public static UserFileManager getInstance() {
        if (instance == null) {
            instance = new UserFileManager();
        }
        return instance;
    }

    public File getFile(String file) throws ServerError {
        return chatClient.downloadFile(file);
    }
}
