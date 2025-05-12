package model;

import mediator.ChatClient;
import util.ServerError;

import java.io.File;

public class UserFilesManager {

    static private UserFilesManager instance;

    static public final String DOWNLOADS_DIRECTORY = "downloads";

    private ChatClient chatClient = ChatClient.getInstance();

    public static UserFilesManager getInstance() {
        if (instance == null) {
            instance = new UserFilesManager();
        }
        return instance;
    }

    public File getFile(String file) throws ServerError {
        File f = new File(DOWNLOADS_DIRECTORY + "/" + file);

        // Send filen hvis den allerede er hentet
        if (f.isFile()) return f;

        return chatClient.downloadFile(file);
    }
}
